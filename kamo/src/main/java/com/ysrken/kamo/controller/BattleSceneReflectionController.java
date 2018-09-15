package com.ysrken.kamo.controller;

import com.ysrken.kamo.MainApp;
import com.ysrken.kamo.model.BattleSceneReflectionModel;
import com.ysrken.kamo.model.SceneHelperModel;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
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
     * Model
     */
    @Autowired
    BattleSceneReflectionModel model;

    /**
     * 初期化
     */
    public void initialize() {
        System.out.println("DEBUG MainApp - BattleSceneReflectionController#initialize");

        // SceneTabを自動生成する
        final ObservableList<Tab> tabList = SceneTabs.getTabs();
        tabList.addAll(model.getSceneTabList());
    }

    /**
     * 画像をタブにセット
     * @param key タブ名
     * @param image 画像
     */
    public void setImage(String key, BufferedImage image){
        model.setImage(key, image);
    }
}
