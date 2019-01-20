package com.ysrken.kamo.model;

import com.ysrken.kamo.MainApp;
import com.ysrken.kamo.controller.SceneTab;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

@Component
public class BattleSceneReflectionModel {

    /**
     * 画像追加ルーチン用のMap
     */
    final private Map<String, Consumer<BufferedImage>> setImageFunc = new HashMap<>();

    /**
     * 表示するシーン一覧
     */
    final static public Set<String> SceneList = new LinkedHashSet<>(
            Arrays.asList("昼戦後", "夜戦後", "戦闘後", "戦闘結果", "MVP", "マップ", "中破", "大破", "ドロップ")
    );

    /**
     * コンストラクタ
     */
    public BattleSceneReflectionModel(){ }

    /**
     * シーン表示用タブを生成して返す
     * @return シーン表示用タブの一覧
     */
    public List<Tab> getSceneTabList(){
        List<Tab> tabList = new ArrayList<>();
        for(String scene : BattleSceneReflectionModel.SceneList) {
            try{
                /**
                 * 「SpringのDIに対応したオリジナルなタブコンポーネント生成」
                 * のために手順がややこしい……
                 */
                final FXMLLoader loader = new FXMLLoader();
                loader.setControllerFactory(MainApp.getApplicationContext()::getBean);
                SceneTab sceneTab = MainApp.getApplicationContext().getBean(SceneTab.class);
                loader.setRoot(sceneTab);
                loader.setController(sceneTab);
                loader.load(getClass().getResourceAsStream("/fxml/SceneTab.fxml"));
                sceneTab.initialize(scene);

                // 生成したタブをリストに追加
                tabList.add(sceneTab);
                setImageFunc.put(scene, (image) -> sceneTab.setImage(image));
            }catch(IOException e){ }
        }
        return tabList;
    }

    /**
     * 指定したシーンに指定した画像をセットする
     */
    public void setImage(String key, BufferedImage image){
        setImageFunc.get(key).accept(image);
        if (key.equals("昼戦後") || key.equals("夜戦後")) {
            setImageFunc.get("戦闘後").accept(image);
        }
    }
}
