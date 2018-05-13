package com.ysrken.kamo;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class MainController {
    @FXML private MenuItem ExitMenu;
    @FXML private MenuItem AboutMenu;

    public void initialize(){
        // 終了コマンド
        ExitMenu.setOnAction(e -> System.exit(0));
        // バージョン情報コマンド
        AboutMenu.setOnAction(e -> {
            String contentText = String.format("ソフト名：%s%nバージョン：%s%n作者：%s",
                    Utility.getSoftwareName(),
                    Utility.getSoftwareVersion(),
                    Utility.getSoftwareAuthor());
            Utility.ShowDialog(contentText, "バージョン情報");
        });
    }

}
