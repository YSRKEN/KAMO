package com.ysrken.kamo.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FleetCombineModel {

    /** 表示画像 */
    public final List<ObjectProperty<Image>> ImageList = new ArrayList<>();

    /** セルの横個数 */
    public static final int X_COUNT = 4;

    /** セルの縦個数 */
    public static final int Y_COUNT = 4;

    /**
     * コンストラクタ
     */
    public FleetCombineModel(){
        for(int y = 0; y < Y_COUNT; ++y){
            for(int x = 0; x < X_COUNT; ++x){
                ImageList.add(new SimpleObjectProperty<Image>(null));
            }
        }
    }
}
