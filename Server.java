import java.util.*;
import java.util.function.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import com.sun.net.httpserver.*;

import org.jfugue.midi.*;
import org.jfugue.pattern.*;
import org.jfugue.player.*;
import org.jfugue.rhythm.*;

public class Server {
    // Port number used to connect to this server
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));
    // Multiplier for the length of the generated music
    private static final int REPETITIONS = 4;
    // Music generation features
    private static final List<Function<LanguageGenerator, PatternProducer>> FEATURES = List.of(
        // Rhythm
        g -> {
            Rhythm rhythm = new Rhythm();
            for (String instrument : List.of("bassdrum", "snare", "crash", "claps")) {
                String result = "";
                for (int i = 0; i < REPETITIONS; i += 1) {
                    result += g.generate(instrument);
                }
                rhythm.addLayer(result);
            }
            return rhythm.getPattern().setVoice(0).repeat(2);
        },
        // Melody
        g -> new Pattern(compose(g, "measure")).setInstrument("clarinet").setVoice(1),
        // Chord
        g -> new Pattern(compose(g, "chordmeasure")).setInstrument("electric_piano").setVoice(2)
    );

    private static String compose(LanguageGenerator g, String target) {
        String[] parts = new String[REPETITIONS];
        for (int i = 0; i < parts.length; i += 1) {
            parts[i] = g.generate(target);
        }
        return String.join(" ", parts);
    }

    public static void main(String[] args) throws IOException {
        LanguageGenerator generator = new LanguageGenerator(LanguageGenerator.Grammar.MUSIC);
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", (HttpExchange t) -> {
            String html = Files.readString(Paths.get("index.html"));
            send(t, "text/html; charset=utf-8", html);
        });
        server.createContext("/generate", (HttpExchange t) -> {
            Pattern pattern = new Pattern();
            for (Function<LanguageGenerator, PatternProducer> feature : FEATURES) {
                pattern.add(feature.apply(generator));
            }
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            MidiFileManager.savePatternToMidi(pattern, data);
            send(t, "audio/midi", data.toByteArray());
        });
        server.setExecutor(null);
        server.start();
    }

    private static void send(HttpExchange t, String contentType, String data)
            throws IOException, UnsupportedEncodingException {
        byte[] response = data.getBytes("UTF-8");
        send(t, contentType, response);
    }

    private static void send(HttpExchange t, String contentType, byte[] response)
            throws IOException, UnsupportedEncodingException {
        t.getResponseHeaders().set("Content-Type", contentType);
        t.sendResponseHeaders(200, response.length);
        try (OutputStream os = t.getResponseBody()) {
            os.write(response);
        }
    }
}
