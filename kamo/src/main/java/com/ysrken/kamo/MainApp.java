package com.ysrken.kamo;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import com.ysrken.kamo.stage.ExtraStage;
import com.ysrken.kamo.stage.ExtraStageFactory;
import static com.ysrken.kamo.Constant.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

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
    	context = new AnnotationConfigApplicationContext(MainApp.class);
        launch(args);
    }

    /**
     * JavaFXの起動処理
     * @param stage Stage情報
     * @throws Exception 実行時例外
     */
    public void start(Stage stage) throws Exception {
		ExtraStage mainStage = eFactory.create(stage, "/fxml/main.fxml", "MainWindow");
        mainStage.setTitle(SOFTWARE_NAME);
        mainStage.setWidth(400);
        mainStage.setHeight(250);
        mainStage.setOnCloseRequest(() -> {
        	Platform.exit();
            System.exit(0);
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
