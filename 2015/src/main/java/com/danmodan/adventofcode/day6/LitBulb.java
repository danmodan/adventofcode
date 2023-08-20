package com.danmodan.adventofcode.day6;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.danmodan.adventofcode.common.config.CommonConfig;
import com.danmodan.adventofcode.common.stream.DataSourceSession;
import com.danmodan.adventofcode.day6.model.Action;
import com.danmodan.adventofcode.day6.model.BrightnessGrid;
import com.danmodan.adventofcode.day6.model.BulbGrid;
import com.danmodan.adventofcode.day6.model.Command;
import com.danmodan.adventofcode.day6.model.Point;

@Configuration
@ComponentScan("com.danmodan.adventofcode.day6")
@PropertySource("classpath:day6/application.properties")
@Import(CommonConfig.class)
public class LitBulb  {

    public static void main(String[] args) throws Exception {

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(LitBulb.class)) {
            context.registerShutdownHook();

            DataSourceSession session = context.getBean(DataSourceSession.class);
            BulbGrid bulbGrid = context.getBean(BulbGrid.class);
            BrightnessGrid brightnessGrid = context.getBean(BrightnessGrid.class);
            ResultService resultService = context.getBean(ResultService.class);

            Consumer<Command> consumerFunc = (Command command) -> {
                bulbGrid.process(command);
                brightnessGrid.process(command);
            };

            BinaryOperator<Void> resultProcessor = (void1, void2) -> {
                resultService.process(context.getEnvironment().getProperty("output.file-path"));
                return null;
            };

            CompletableFuture
                .runAsync(session::connect)
                .thenCombine(CompletableFuture.runAsync(() -> session.performOnData(consumerFunc)), resultProcessor)
                .join();
        }
    }

    @Bean
    public DataSourceSession dataSourceSession(
            @Value("${input.file-path}") String inputFilePath,
            @Value("${input.line-pattern}") Pattern pattern,
            ResourceLoader resourceLoader) throws IOException {

        return DataSourceSession
                .readStream()
                    .fromFile(resourceLoader.getResource(inputFilePath).getURI())
                .dataUnit()
                    .asLine(line -> {
                        Matcher matcher = pattern.matcher(line);
                        matcher.find();
                        Action action = Action.getByCode(matcher.group());
                        matcher.find();
                        String[] xy1 = matcher.group().split(",");
                        matcher.find();
                        String[] xy2 = matcher.group().split(",");
                        Point point1 = new Point(Integer.parseInt(xy1[0]), Integer.parseInt(xy1[1]));
                        Point point2 = new Point(Integer.parseInt(xy2[0]), Integer.parseInt(xy2[1]));
                        return new Command(action, point1, point2);
                    })
                .readDataDestiny()
                    .toBlockingQueue(100)
                .load();
    }

    @Bean
    public BulbGrid bulbGrid(@Value("${grid-size}") int gridSize) {

        return new BulbGrid(gridSize);
    }

    @Bean
    public BrightnessGrid brightnessGrid(@Value("${grid-size}") int gridSize) {

        return new BrightnessGrid(gridSize);
    }
}

@Service
class ResultService {

    private static final Logger log = Logger.getLogger(ResultService.class.getName());

    private final BulbGrid bulbGrid;
    private final BrightnessGrid brightnessGrid;

    ResultService(BulbGrid bulbGrid, BrightnessGrid brightnessGrid) {
        this.bulbGrid = bulbGrid;
        this.brightnessGrid = brightnessGrid;
    }

    void process(String outputFilePath) {

        log.log(Level.INFO, () -> "Lit lights: " + bulbGrid.countLitBulbs());
        log.log(Level.INFO, () -> "Total brightness: " + brightnessGrid.countBulbsBrightness());

        try (BufferedWriter bw = new BufferedWriter(new PrintWriter(outputFilePath))) {
            int gridLength = bulbGrid.getGrid().length;

            for (int i = gridLength - 1; i >= 0; i--) {

                String line = bulbGrid.getGrid()[i]
                        .toString()
                        .substring(0, gridLength)
                        .replace('0', '.')
                        .replace('1', '#');

                bw.write(line);
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            log.log(Level.SEVERE, e, () -> "deu ruim");
        }
    }
}
