package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.function.Function;

public interface Reader {

    void read(BufferedReader reader, Producer producer) throws IOException, InterruptedException;

    class LineReader implements Reader {

        private final Function<String, ?> createMessage;
    
        public LineReader(Function<String, ?> createMessage) {
            this.createMessage = createMessage;
        }
    
        @Override
        public void read(BufferedReader reader, Producer producer) throws IOException, InterruptedException {
    
            String line = null;
    
            while ((line = reader.readLine()) != null) {
    
                Object msg = createMessage.apply(line);
                producer.enqueueMessage(msg);
            }
        }
    }

    class CharacterReader implements Reader {

        private static final char EOF = (char) -1;
        private static final char LF = '\n';
        private static final char CR = '\r';
    
        private final Function<Character, ?> createMessage;
    
        public CharacterReader(Function<Character, ?> createMessage) {
            this.createMessage = createMessage;
        }
    
        @Override
        public void read(BufferedReader reader, Producer producer) throws IOException, InterruptedException {
    
            char character = 0;
    
            while ((character = (char) reader.read()) != EOF) {
    
                if (character == LF || character == CR) {
                    continue;
                }
    
                Object msg = createMessage.apply(character);
                producer.enqueueMessage(msg);
            }
        }
    }
}
