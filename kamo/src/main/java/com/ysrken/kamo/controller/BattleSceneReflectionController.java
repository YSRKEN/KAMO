package com.ysrken.kamo.controller;

import com.ysrken.kamo.MainApp;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

/**
 * 戦闘振り返り画面のController
 * @author ysrken
 */
@Component
public class BattleSceneReflectionController {
    /**
     * タブ一覧(コントロール)
     */
    @FXML
    private TabPane SceneTabs;
    /**
     * 表示するシーン一覧
     */
    final private Set<String> sceneList = new LinkedHashSet<>(
            Arrays.asList("昼戦後", "夜戦後", "戦闘結果", "MVP", "マップ")
    );
    /**
     * タブ一覧(ハッシュアクセス用)
     */
    private Map<String, SceneTab> tabMap = new HashMap<>();

    /**
     * 初期化
     */
    public void initialize() {
        System.out.println("DEBUG MainApp - BattleSceneReflectionController#initialize");

        // SceneTabを自動生成する
        final ObservableList<Tab> tabList = SceneTabs.getTabs();
        for(String scene : sceneList) {
            try{
                final FXMLLoader loader = new FXMLLoader();
                loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
                SceneTab sceneTab = MainApp.getApplicationContext().getBean(SceneTab.class);
                loader.setRoot(sceneTab);
                loader.setController(sceneTab);
                loader.load(getClass().getResourceAsStream("/fxml/SceneTab.fxml"));
                sceneTab.initialize(scene);
                tabList.add(sceneTab);
                tabMap.put(scene, sceneTab);
            }catch(IOException e){
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }
    }

    /**
     * 画像をタブにセット
     * @param key タブ名
     * @param image 画像
     */
    public void setImage(String key, BufferedImage image){
        final SceneTab targetTab = tabMap.get(key);
        targetTab.setImage(image);
    }
    /**
     * 表示するシーン一覧
     * @return 表示するシーン一覧
     */
    public Set<String> getBattleSceneSet(){
        return sceneList;
    }
}
