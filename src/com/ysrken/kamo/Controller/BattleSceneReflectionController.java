package com.ysrken.kamo.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.TabPane;

import java.awt.image.BufferedImage;
import java.util.*;

public class BattleSceneReflectionController {
    /**
     * タブ一覧(コントロール)
     */
    @FXML private TabPane SceneTabs;
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
    public void initialize(){
        // SceneTabを自動生成する
        final var tabList = SceneTabs.getTabs();
        for(var scene : sceneList) {
            final var sceneTab = new SceneTab(scene);
            tabList.add(sceneTab);
            tabMap.put(scene, sceneTab);
        }
    }
    /**
     * 画像をタブにセット
     * @param key タブ名
     * @param image 画像
     */
    public void setImage(String key, BufferedImage image){
        final var targetTab = tabMap.get(key);
        targetTab.setImage(image);
    }
    /**
     * テキストをタブにセット
     * @param key タブ名
     * @param text テキスト
     */
    public void setText(String key, String text){
        final var targetTab = tabMap.get(key);
        targetTab.setLabelText(text);
    }
    /**
     * 表示するシーン一覧
     * @return 表示するシーン一覧
     */
    public Set<String> getBattleSceneSet(){
        return sceneList;
    }
}
