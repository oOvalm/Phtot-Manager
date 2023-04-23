package com.demo.photomanage.controller;

import com.demo.photomanage.container.PlayData;
import com.demo.photomanage.utils.GenerateDialog;
import com.demo.photomanage.utils.GlobalValue;
import com.demo.photomanage.utils.Tools;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import java.io.File;
import java.security.Key;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javafx.util.Duration;
import org.controlsfx.control.Notifications;

import static java.lang.Math.abs;

public class ImageViewController {
    @FXML
    private BorderPane borderpane;
    @FXML
    private ImageView imageview;
    @FXML
    private HBox buttonbox;
    @FXML
    private Button nextimage;

    @FXML
    private Button playppt;

    @FXML
    private StackPane imagepane;

    @FXML
    private Button previmage;

    @FXML
    private Button zoomin;

    @FXML
    private Button zoomout;

    private File directory;
    private Stage stage;
    private Image curimage;
    private int curimageidx;
    private ArrayList<File> images = new ArrayList<>(); // 原项目用的是自写类
    private long TimeStamp = 0;
    private final static double GAP = 20;   // 图片和边缘的距离gap/2 好像有问题
    @FXML
    public void initialize(){}
    public void init(Stage stage, String path){
        this.stage = stage;
        autoAdapt();
//        System.out.println(stage.getWidth());
//        System.out.println(stage.getHeight());
//        System.out.println(stage.widthProperty());
//        System.out.println(stage.heightProperty());
        File file = new File(path);
        directory = file.getParentFile();
        TimeStamp = directory.lastModified();
        curimage = new Image(path);
        images.clear();
        images = Tools.getAvailableImageFiles(directory.listFiles());
        curimageidx = -1;
        for(int i = 0; i < images.size(); i++){
            if(file.equals(images.get(i)))curimageidx = i;
        }
        updateImageView();

    }
    private void updateImageView(){
//        if(curimage == null){
//            System.out.println("curimage is null");
//        }
        if(images.size() == 0){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("该文件夹下无图片");
            alert.showAndWait();
            stage.close();
        }
        curimage = new Image(images.get(curimageidx).getPath());
        imageview.getTransforms().clear();
        imageview.setImage(curimage);
        stage.setTitle(Tools.getImageName(curimage));
        double width = curimage.getWidth(), height = curimage.getHeight();
        System.out.println(imagepane.widthProperty());
        System.out.println(imagepane.heightProperty());
        // 放得下直接放
        if(width <= imagepane.getWidth()-GAP && height <= imagepane.getHeight()-GAP){
            imageview.fitWidthProperty().bind(imagepane.widthProperty());
            imageview.fitHeightProperty().bind(imagepane.heightProperty());
        }
        else{   // 放不下限制长宽最大大小
            imageview.fitWidthProperty().bind(imagepane.widthProperty().subtract(GAP));
            imageview.fitHeightProperty().bind(imagepane.heightProperty().subtract(GAP));
        }
    }
    /**
     * 更新图片列表，将原图片位置匹配新图片位置(curimageidx 必须是有效的)
     */
    private void updateImageArray(int op) {
        if(directory.lastModified() == TimeStamp)return;
        TimeStamp = directory.lastModified();
        ArrayList<File> original = new ArrayList<>(images);
        images.clear();
        images = Tools.getAvailableImageFiles(directory.listFiles());
        int newidx = images.indexOf(original.get(curimageidx));
        if(newidx != -1){
            curimageidx = newidx;
        }
        else{
            // TODO: 用最佳匹配位置代替开头(有点hard)
            curimageidx = -op;
            Notifications.create()
                    .text("丢失图片位置，已回到第一张图片位置")
                    .owner(stage)
                    .position(Pos.TOP_CENTER)
                    .hideAfter(Duration.seconds(5))
                    .show();
        }
    }

    /**
     * 自适应(may solved)
     */
    private void autoAdapt(){
        // 初始化执行
//        imagepane.setPrefSize(stage.getWidth(), stage.getHeight()*0.8);
//        borderpane.setPrefSize(stage.getWidth(), stage.getHeight());
//        buttonbox.setPrefSize(stage.getWidth(), stage.getHeight()*0.2);
//        System.out.println(imagepane.getPrefHeight());
//        System.out.println(imagepane.getPrefWidth());
        borderpane.prefHeightProperty().bind(stage.heightProperty());
        borderpane.prefWidthProperty().bind(stage.widthProperty());
        buttonbox.prefHeightProperty().bind(stage.heightProperty().multiply(0.2));
        buttonbox.prefWidthProperty().bind(stage.widthProperty());
        imagepane.prefWidthProperty().bind(stage.widthProperty());
        imagepane.prefHeightProperty().bind(stage.heightProperty().multiply(0.8));
        // Listener
//        stage.heightProperty().addListener((observable, oldValue, newValue)->{
//            double val = newValue.doubleValue();
//            buttonbox.setPrefHeight(val * 0.2);
//            imagepane.setPrefHeight(val * 0.8);
//        });
//        stage.widthProperty().addListener((observable, oldValue, newValue)->{
//            double val = newValue.doubleValue();
//            buttonbox.setPrefWidth(val);
//            imagepane.setPrefWidth(val);
////            System.out.println("width: stage:"+val+"  imagepane:"+imagepane.getPrefWidth());
//        });
    }

    /**
     * 还不知道挂那个控件上
     */
    @FXML
    private void KeyPressed(KeyEvent e){
        System.out.println("wwow");
        System.out.println(e.getCode());
        if(e.getCode() == KeyCode.LEFT){
            PrevImage();
        }
        else if(e.getCode() == KeyCode.RIGHT){
            NextImage();
        }
    }
    @FXML
    private void NextImage() {
        curimageidx++;
        if(curimageidx == images.size()){
            curimageidx = 0;
            Notifications.create()
                    .text("这是第一张")
                    .owner(stage)
                    .position(Pos.TOP_CENTER)
                    .hideAfter(Duration.seconds(3))
                    .show();
        }
        updateImageView();
    }
    @FXML
    private void PrevImage() {
        curimageidx--;
        if(curimageidx < 0){
            curimageidx = images.size()-1;
            Notifications.create()
                    .text("这是最后一张")
                    .owner(stage)
                    .position(Pos.TOP_CENTER)
                    .hideAfter(Duration.seconds(3))
                    .show();
        }
        updateImageView();
    }

    @FXML
    private void ZoomIn() {
        // 1.1 1.1放大，中心为图片中心(也有可能是上半部分窗口中心)
        imageview.getTransforms().add(
                new Scale(1.1, 1.1, imageview.prefWidth(-1) / 2, imageview.prefHeight(-1) / 2));
    }

    @FXML
    private void ZoomOut() {
        imageview.getTransforms().add(
                new Scale(0.9, 0.9, imageview.prefWidth(-1) / 2, imageview.prefHeight(-1) / 2));
    }
    @FXML
    private void Play() {
        Dialog<PlayData> dialog = GenerateDialog.NewPlayDialog();
        Optional<PlayData> optional = dialog.showAndWait();
        if(optional.isPresent()) {
            PlayData data = optional.get();
            playIt(data.getTime());
        }
    }
    public void PlayByMain(){
        Dialog<PlayData> dialog = GenerateDialog.NewPlayDialog();
        Optional<PlayData> optional = dialog.showAndWait();
        if(optional.isPresent()){
            stage.show();
            stage.setMaximized(true);
            playIt(optional.get().getTime());
        }
        else{
            // 直接结束
            stage.close();
        }
    }
    private void playIt(double gap){
        boolean oriView = stage.isMaximized();
        stage.setMaximized(true);   // 最大化
        imagepane.requestFocus();   // 设置焦点
        updateImageView();
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(gap), e->{
            imagepane.requestFocus();
            NextImage();
            updateImageView();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);    // 循环无限次
        timeline.play();
        imagepane.setOnKeyPressed(e->{
            if(e.getCode() == KeyCode.ESCAPE){
                timeline.stop();
                stage.setMaximized(oriView);
                imagepane.setOnKeyPressed(ee->{});
            }
            else if(e.getCode() == KeyCode.LEFT){
                PrevImage();
                updateImageView();
                timeline.play();    // 重置计时
            }
            else if(e.getCode() == KeyCode.RIGHT){
                timeline.stop();
                NextImage();
                updateImageView();
                timeline.play();
            }
        });
    }
}