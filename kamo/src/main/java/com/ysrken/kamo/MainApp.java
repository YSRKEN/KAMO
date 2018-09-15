package com.ysrken.kamo;

import com.ysrken.kamo.service.SceneRecognitionService;
import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.ysrken.kamo.Constant.SOFTWARE_NAME;

/**
 * 起動用クラス
 * @author ysrken
 */
@ComponentScan
public class MainApp extends Application {
    private static ConfigurableApplicationContext context;
    
    private ExtraStageFactory eFactory = new ExtraStageFactory();

    /**
     * main関数
     * @param args コマンドライン引数
     * @throws Exception 実行時例外
     */
    public static void main(String[] args) throws Exception {
        System.out.println("DEBUG MainApp - MainApp#main");
    	context = new AnnotationConfigApplicationContext(MainApp.class);
        launch(args);
    }

    /**
     * JavaFXの起動処理
     * @param stage Stage情報
     * @throws Exception 実行時例外
     */
    public void start(Stage stage) throws Exception {
    	System.out.println("DEBUG MainApp - MainApp#start");
		ExtraStage mainStage = eFactory.create(stage, "/fxml/MainView.fxml", "MainWindow");
        mainStage.setTitle(SOFTWARE_NAME);
        mainStage.setOnCloseRequest(() -> {
        	Platform.exit();
            System.exit(0);
        });
        mainStage.setOnDragOver();
        mainStage.setOnDragDropped((File file) -> {
            try {
                BufferedImage image = ImageIO.read(file);
                SceneRecognitionService sceneRecognition = context.getBean(SceneRecognitionService.class);
                sceneRecognition.testSceneRecognition(image);
            }catch (IOException e){
                e.printStackTrace();
            }
        });
        mainStage.show();
    }
    
    /**
     * ApplicationContextを引き回すために使用
     * @return 共用するApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
    	return context;
    }
    
    /**
     * JavaFXの終了処理
     * @throws Exception 実行時例外
     */
    @Override
    public void stop() throws Exception {
        context.close();
    }
}
