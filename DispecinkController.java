package com.example.plgdistribuce;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.*;

public class DispecinkController {

    @FXML private VBox autaContainer;
    @FXML private Label distribuceDenText;

    @FXML private ScrollPane routesContainerScrool;
    @FXML public VBox routesContainer;
    @FXML private Button csvButton;
    @FXML private Button buttonUzivTrasy;
    @FXML private Button tabulkaProSkladButton;
    @FXML private Button denDochazkyButton;
    @FXML private ProgressIndicator mainProgress;
    @FXML private VBox mainProgressBox;
    @FXML private Label mainProgressText;

    private List<Label> cardLabelAuta;
    private List<Label> cardLabelPosadky;
    private MyData mojeAplikacniData;
    private List<MyRoute> nacteneTrasy;
    private List<ImportDataObchod> dataOz;
    private List<ImportDataObchod> dataOblast;
    private LocalDateTime casPosledniAktualizaceObchod;
    private boolean cekejteProsim;
    private boolean tabulkaProSklad;
    private int hlavniTabulkaMod;

    @FXML
    public void initialize() {
        cekejteProsim = false;
        hlavniTabulkaMod = 0;
        setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
        dataOz = new ArrayList<>();
        dataOblast = new ArrayList<>();
        routesContainer.getChildren().clear();
        tabulkaProSklad = true;
        mojeAplikacniData = MyData.get();
        cardLabelAuta = new ArrayList<>();
        cardLabelPosadky = new ArrayList<>();
        setCardCars();
        nacteneTrasy = RouteManager.get().getTrasy();
        buttonUzivTrasyText();
        distribuceDenTextAktualizovat();
        denDochazky();
    }

    public void setCardCars(){
        mojeAplikacniData = MyData.get();
        cardLabelAuta.clear();
        cardLabelPosadky.clear();
        autaContainer.getChildren().clear();

        HBox box = null;
        for (int i = 0; i < mojeAplikacniData.getAuta().size(); i++) {

            if (box == null) {
                box = new HBox();
                HBox.setHgrow(box, Priority.ALWAYS);
                box.setMaxWidth(Double.MAX_VALUE);
                box.setSpacing(10);
                autaContainer.getChildren().add(box);
            }

            VBox karta = new VBox();
            HBox.setHgrow(karta, Priority.ALWAYS);
            karta.setMaxWidth(Double.MAX_VALUE);
          //  karta.prefWidthProperty().bind(box.widthProperty().divide(2));
            karta.setMinWidth(0);
            karta.setPrefWidth(1);
            karta.setMinWidth(0);
            karta.getStyleClass().add("posadka-karta");
            box.getChildren().add(karta);

            Label nazev = new Label("POSÁDKA / SPZ");
            nazev.getStyleClass().add("dispecink-karta-nadpis");
            karta.getChildren().add(nazev);

            HBox vnitrekKarty = new HBox();
            vnitrekKarty.setSpacing(10);
            vnitrekKarty.setAlignment(Pos.CENTER_LEFT);
            karta.getChildren().add(vnitrekKarty);

            VBox textPosadkaZnacka = new VBox();
            HBox.setHgrow(textPosadkaZnacka, Priority.ALWAYS);
            textPosadkaZnacka.setMaxWidth(Double.MAX_VALUE);
            textPosadkaZnacka.setMinWidth(0);
            textPosadkaZnacka.setPrefWidth(1);
            vnitrekKarty.getChildren().add(textPosadkaZnacka);

            Label lblPosadka = new Label("Posadka");
            lblPosadka.getStyleClass().add("dispecink-karta-posadka");
            lblPosadka.setMinWidth(0); // Dovolí labelu zmenšit se pod délku textu
            lblPosadka.setMaxWidth(Double.MAX_VALUE); // Dovolí labelu roztáhnout se podle karty
            lblPosadka.setTextOverrun(OverrunStyle.ELLIPSIS);
            lblPosadka.setPrefWidth(1); // Přinutí Label spolehnout se plně na rozměr VBoxu
            lblPosadka.setTextOverrun(OverrunStyle.ELLIPSIS);
            textPosadkaZnacka.getChildren().add(lblPosadka);
            cardLabelPosadky.add(lblPosadka);

            Label lblZnacka = new Label("Auto");
            lblZnacka.getStyleClass().add("dispecink-karta-auto");
            textPosadkaZnacka.getChildren().add(lblZnacka);
            cardLabelAuta.add(lblZnacka);

            Button ridic = new Button();
            ridic.getStyleClass().add("karta-tlacitko");
            ridic.setText("Řidič");
            ridic.setMinWidth(Region.USE_PREF_SIZE);
            ridic.setMaxWidth(Region.USE_PREF_SIZE);
            vnitrekKarty.getChildren().add(ridic);

            Button zavoznik = new Button();
            zavoznik.getStyleClass().add("karta-tlacitko-sec");
            zavoznik.setText("Závozník");
            zavoznik.setMinWidth(Region.USE_PREF_SIZE);
            zavoznik.setMaxWidth(Region.USE_PREF_SIZE);
            vnitrekKarty.getChildren().add(zavoznik);

            int finalIndex = i;
            ridic.setOnAction(event -> {
                if (cekejteProsim){
                    Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                    DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
                    return;
                }
                setPosadka(finalIndex, true);
                setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
                routesContainer.getChildren().clear();
            });

            zavoznik.setOnAction(event -> {
                if (cekejteProsim){
                    Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                    DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
                    return;
                }
                setPosadka(finalIndex, false);
                setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
                routesContainer.getChildren().clear();
            });

            if (box.getChildren().size() ==  2) {
                box = null;
            }

        }
        if (box != null && box.getChildren().size() == 1) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            spacer.setMaxWidth(Double.MAX_VALUE);
            spacer.setMinWidth(0);
            spacer.setPrefWidth(1);

            box.getChildren().add(spacer);
        }
        setTextCardCars();
    }

    private void setPosadka(int indexPosadky, boolean ridic) {

        if (ridic) {
            if (indexPosadky >= 0 && indexPosadky < cardLabelPosadky.size() && indexPosadky < mojeAplikacniData.getAuta().size()) {
                if (mojeAplikacniData.getPosadka().get(indexPosadky).getZavoznik() != null){
                    mojeAplikacniData.getPosadka().get(indexPosadky).setZavoznik(null);
                    setTextCardCars();
                    priraditPosadku();
                    return;
                }
                List<MyData.RidicZavoznik> seznamRidicu;
                if (mojeAplikacniData.getAuta().get(indexPosadky).getZnacka().toUpperCase().contains("BOXER".toUpperCase())) seznamRidicu = mojeAplikacniData.getZavoznici();
                else seznamRidicu = mojeAplikacniData.getRidici();
                String aktualniRidic = "";
                if (mojeAplikacniData.getPosadka().get(indexPosadky).getRidic() != null) aktualniRidic = mojeAplikacniData.getPosadka().get(indexPosadky).getRidic().getJmeno();

                int aktualniIndex = -1;
                if (!aktualniRidic.isEmpty()) {
                    for (int i = 0; i < seznamRidicu.size(); i++) {
                        if (seznamRidicu.get(i).getJmeno().equals(aktualniRidic)) {
                            aktualniIndex = i;
                            break;
                        }
                    }
                }
                int celkovyPocetStavu = seznamRidicu.size() + 1;
                int novyIndexStavu = (aktualniIndex + 1 + 1) % celkovyPocetStavu - 1;
                if (novyIndexStavu == -1) { mojeAplikacniData.getPosadka().get(indexPosadky).setRidic(null);}
                else { mojeAplikacniData.getPosadka().get(indexPosadky).setRidic(seznamRidicu.get(novyIndexStavu)); }
                mojeAplikacniData.getPosadka().get(indexPosadky).setZavoznik(null);
            }
        }
        else {
            MyData.RidicZavoznik aktualniRidic = mojeAplikacniData.getPosadka().get(indexPosadky).getRidic();
            if (aktualniRidic == null) return;

            List<MyData.RidicZavoznik> seznamZavoznicu = mojeAplikacniData.getZavoznici();

            int aktualniIndex = -1;
            if (mojeAplikacniData.getPosadka().get(indexPosadky).getZavoznik() != null) {
                String jmenoZavoznika = mojeAplikacniData.getPosadka().get(indexPosadky).getZavoznik().getJmeno();
                for (int i = 0; i < seznamZavoznicu.size(); i++) {
                    if (seznamZavoznicu.get(i).getJmeno().equals(jmenoZavoznika)) {
                        aktualniIndex = i;
                        break;
                    }
                }
            }

            int novyIndex = aktualniIndex;
            for (int i = 0; i <= seznamZavoznicu.size(); i++) {
                novyIndex = (novyIndex + 1) % (seznamZavoznicu.size() + 1);

                if (novyIndex == seznamZavoznicu.size()) {
                    mojeAplikacniData.getPosadka().get(indexPosadky).setZavoznik(null);
                    break;
                }

                MyData.RidicZavoznik kandidat = seznamZavoznicu.get(novyIndex);
                if (!kandidat.getJmeno().equals(aktualniRidic.getJmeno())) {
                    mojeAplikacniData.getPosadka().get(indexPosadky).setZavoznik(kandidat);
                    break;
                }
            }
        }
        setTextCardCars();
        priraditPosadku();
    }

    private void priraditPosadku(){
        for (MyRoute trasa : nacteneTrasy) {
            String testZmeny = trasa.posadka;
            for (MyData.Posadka p : mojeAplikacniData.getPosadka()) {
                if (trasa.spz.toUpperCase().equals(p.getSpz().toUpperCase())) {
                    if (p.getRidic() != null) {
                        trasa.posadka = "";
                        if (p.getAuto().toUpperCase().contains("BOXER")) trasa.posadka = "BOXER ";
                        trasa.posadka += p.getRidic().getJmenoUp();
                        trasa.telefon = p.getRidic().getTelefon();
                        if (p.getZavoznik() != null) {
                            trasa.posadka = trasa.posadka + " + " + p.getZavoznik().getJmenoUp();
                        }
                        break;
                    }
                }
            }
            if (trasa.factory == "1" && !trasa.posadka.equals(testZmeny)){
                DialogManager.showConfirmDialog((Stage) routesContainer.getScene().getWindow(), "Změnili jste posádku u vlastní trasy.\n" + testZmeny + " na " + trasa.posadka, true, false, false);
            }
        }
    }

    public void setTextCardCars(){
        mojeAplikacniData = MyData.get();
        for (int i = 0; i < cardLabelAuta.size(); i++) {
            if (i < mojeAplikacniData.getAuta().size()) {
                cardLabelAuta.get(i).setText(mojeAplikacniData.getAuta().get(i).getZnacka() + "  |  " + mojeAplikacniData.getAuta().get(i).getSpz());
            }
            else cardLabelAuta.get(i).setText("Auto");
        }
        for (int i = 0; i < cardLabelPosadky.size(); i++) {
            cardLabelPosadky.get(i).getStyleClass().remove("dispecink-karta-posadka-no");
            if (i < mojeAplikacniData.getPosadka().size()) {
                if (mojeAplikacniData.getPosadka().get(i).getRidic() != null) {
                    cardLabelPosadky.get(i).setText(mojeAplikacniData.getPosadka().get(i).getRidic().getJmeno());
                    if (mojeAplikacniData.getPosadka().get(i).getZavoznik() != null) {
                        cardLabelPosadky.get(i).setText(mojeAplikacniData.getPosadka().get(i).getRidic().getJmeno() + " + " + mojeAplikacniData.getPosadka().get(i).getZavoznik().getJmeno());
                    }
                }
                else {
                    cardLabelPosadky.get(i).setText("Posádka");
                    cardLabelPosadky.get(i).getStyleClass().add("dispecink-karta-posadka-no");
                }
            }
            else {
                cardLabelPosadky.get(i).setText("Posádka");
                cardLabelPosadky.get(i).getStyleClass().add("dispecink-karta-posadka-no");
            }
        }
    }

    @FXML
    public void tabulkaProSkladZmena(){
        if (cekejteProsim){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
            return;
        }
        tabulkaProSklad = !tabulkaProSklad;
        if (tabulkaProSklad) tabulkaProSkladButton.setText("EX");
        else tabulkaProSkladButton.setText("OZ");
        setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
        routesContainer.getChildren().clear();
    }

    @FXML
    public void denDochazky(){
        if (cekejteProsim){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
            return;
        }
        LocalDate datum;
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("eeee dd.MM.yyyy")
                .toFormatter(new Locale("cs", "CZ"));

        try {
            datum = LocalDate.parse(denDochazkyButton.getText(), formatter);
        } catch (DateTimeParseException e) {
            datum = LocalDate.now();
        }
        if (datum.isAfter(LocalDate.now().plusDays(10))) {
            datum = LocalDate.now();
        }

        datum = datum.plusDays(1);

        denDochazkyButton.setText(datum.format(formatter).substring(0, 1).toUpperCase() + datum.format(formatter).substring(1));
        setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
        routesContainer.getChildren().clear();
    }

    private void writeObchod(List<ImportDataObchod> data, String myDate){

        if (data == null || data.isEmpty()) return;

        if (data == dataOz) {
            String infoText;
            long minutyStary = 60;
            if (casPosledniAktualizaceObchod != null) {
                minutyStary = Duration.between(casPosledniAktualizaceObchod, LocalDateTime.now()).toMinutes();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE dd.MM.yyyy HH:mm", Locale.forLanguageTag("cs-CZ"));
                String datumStrojovo = casPosledniAktualizaceObchod.format(formatter).toUpperCase();
                infoText = "AKTUALIZACE: " + datumStrojovo + ", PŘED " + minutyStary + (minutyStary == 1 ? " MINUTOU." : " MINUTAMI.");
            } else {
                infoText = "ČAS POSLEDNÍ ÚPRAVY SOUBORU NENÍ K DISPOZICI.";
            }

            VBox allNote = new VBox();
            allNote.setFillWidth(true);
            allNote.getStyleClass().add("main-table-box");

            Label textNoteZahlavi = new Label();
            textNoteZahlavi.setAlignment(Pos.CENTER_LEFT);
            textNoteZahlavi.getStyleClass().add("main-table-box-label-left");
            textNoteZahlavi.setText("INFO O AKTUALIZAČNÍM SOUBORU OBJEDNÁVKY.XML");
            allNote.getChildren().addAll(textNoteZahlavi);

            HBox boxNote = new HBox();
            HBox.setHgrow(boxNote, Priority.ALWAYS);
            boxNote.getStyleClass().add("poznamka-box");
            boxNote.setMaxWidth(Double.MAX_VALUE);

            Image smileImage;
            if (minutyStary <= 30) smileImage = new Image(getClass().getResourceAsStream("/com/example/plgdistribuce/smile1.png"));
            else smileImage = new Image(getClass().getResourceAsStream("/com/example/plgdistribuce/smile2.png"));
            ImageView smileView = new ImageView(smileImage);
            smileView.setFitWidth(14);
            smileView.setFitHeight(14);
            HBox.setMargin(smileView, new Insets(5, 0, 0, 10)); // horní, pravé, dolní, levé
            smileView.setPreserveRatio(true);
            boxNote.getChildren().add(smileView);

            Label textNote = new Label();
            textNote.getStyleClass().add("poznamka");
            textNote.getStyleClass().add("poznamka-xml");
            textNote.setText(infoText);
            textNote.setMaxWidth(570);
            textNote.setWrapText(true);
            boxNote.getChildren().add(textNote);
            allNote.getChildren().add(boxNote);

            Region zapatiNote = new Region();
            zapatiNote.setPrefHeight(8);
            HBox.setHgrow(zapatiNote, Priority.ALWAYS);
            allNote.getChildren().add(zapatiNote);

            routesContainer.getChildren().add(allNote);
        }

        VBox tableBox = new VBox();
        tableBox.getStyleClass().add("main-table-box");

        HBox zahlavy = new HBox();

        Label text1 = new Label();
        text1.setAlignment(Pos.CENTER_LEFT);
        text1.getStyleClass().add("main-table-box-label-left");
        text1.setPrefWidth(475);
        if (data == dataOz) text1.setText("PŘEHLED DLE OZ " + myDate.toUpperCase());
        else text1.setText("PŘEHLED DLE OBCHODNÍCH OBLASTÍ " + myDate.toUpperCase());

        Label text2 = new Label();
        text2.setAlignment(Pos.CENTER_RIGHT);
        text2.getStyleClass().add("main-table-box-label-right-oz");
        text2.setPrefWidth(100);
        text2.setText("OBJEM HL");

        zahlavy.getChildren().addAll(text1, text2);

        TableView<ImportDataObchod> table = new TableView<>() {{
            setFocusTraversable(false);
            addEventFilter(ScrollEvent.SCROLL, event -> {
                event.consume();
                if (getParent() != null) {
                    getParent().fireEvent(event);
                }
            });
        }};
        table.getStyleClass().add("main-table");

        TableColumn<ImportDataObchod, String> jmenoCol = new TableColumn<>("Název");
        jmenoCol.getStyleClass().add("main-table-cell-left-oz");
        jmenoCol.setPrefWidth(475);
        jmenoCol.setCellValueFactory(new PropertyValueFactory<>("nazev")); // volá getNazev()

        TableColumn<ImportDataObchod, String> hlCol = new TableColumn<>("Hl celkem");
        hlCol.getStyleClass().add("main-table-cell-right-oz");
        hlCol.setPrefWidth(100);
        hlCol.setCellValueFactory(new PropertyValueFactory<>("hl")); // volá getHl()

        table.getColumns().setAll(jmenoCol, hlCol);

        ObservableList<ImportDataObchod> observableData = FXCollections.observableArrayList(data);
        table.setItems(observableData);

        table.setSelectionModel(null);
        for (TableColumn<?, ?> column : table.getColumns()) { column.setSortable(false); }
        table.setSelectionModel(null);
        table.fixedCellSizeProperty().set(26.0);
        table.prefHeightProperty().bind(Bindings.size(observableData).multiply(table.fixedCellSizeProperty()));

        Region zapati = new Region();
        zapati.setPrefHeight(8);
        HBox.setHgrow(zapati, Priority.ALWAYS);

        tableBox.getChildren().add(zahlavy);
        tableBox.getChildren().add(table);
        tableBox.getChildren().add(zapati);

        routesContainer.getChildren().add(tableBox);
    }

    private void writeDochazka(List<MyData.OsobaDochazka> osobySkladDistribuce){
        List<MyData.OsobaDochazka> osobySeznam = new ArrayList<>();

        for (MyData.OsobaDochazka o: osobySkladDistribuce){
            if (!o.getJmenoUp().isEmpty()) osobySeznam.add(o);
        }

        if (osobySeznam.size() == 0) return;

        String oddeleni;
        if (osobySkladDistribuce == mojeAplikacniData.getOsobySklad()) oddeleni = "SKLAD";
        else oddeleni = "DISTRIBUCE";

        VBox tableBox = new VBox();
        tableBox.getStyleClass().add("main-table-box");

        HBox zahlavy = new HBox();

        Label text1 = new Label();
        text1.getStyleClass().add("main-table-box-label-left");
        text1.setPrefWidth(180);
        text1.setText(oddeleni);

        Label text2 = new Label();
        text2.setAlignment(Pos.CENTER);
        text2.setPrefWidth(130);
        if (oddeleni.equals("SKLAD") && mojeAplikacniData.isRezimSkladu()) text2.setText("NOC");
        else text2.setText("RÁNO");

        Label text3 = new Label();
        text3.setAlignment(Pos.CENTER);
        text3.setPrefWidth(130);
        if (oddeleni.equals("SKLAD") && mojeAplikacniData.isRezimSkladu()) text3.setText("RÁNO");
        else text3.setText("ODPOLEDNE");

        Label text4 = new Label();
        text4.setAlignment(Pos.CENTER);
        text4.setPrefWidth(130);
        if (oddeleni.equals("SKLAD") && mojeAplikacniData.isRezimSkladu()) text4.setText("ODPOLEDNE");
        else text4.setText("NOC");

        zahlavy.getChildren().addAll(text1, text2, text3, text4);

        TableView<MyData.OsobaDochazka> table = new TableView<>() {{
            setFocusTraversable(false);
            addEventFilter(ScrollEvent.SCROLL, event -> {
                event.consume();
                if (getParent() != null) {
                    getParent().fireEvent(event);
                }
            });
        }};
        table.getStyleClass().add("main-table");

        TableColumn<MyData.OsobaDochazka, Void> colOrder = new TableColumn<>("Číslo");
        colOrder.getStyleClass().add("main-table-cell-right");
        colOrder.setPrefWidth(30);
        TableColumn<MyData.OsobaDochazka, String> jmenoUp = new TableColumn<>("jmenoUp");
        jmenoUp.getStyleClass().add("main-table-cell-left");
        jmenoUp.setPrefWidth(150);
        TableColumn<MyData.OsobaDochazka, String> prichod_1 = new TableColumn<>("Čas");
        prichod_1.getStyleClass().add("main-table-cell-center");
        prichod_1.setPrefWidth(65);
        TableColumn<MyData.OsobaDochazka, String> odchod_1 = new TableColumn<>("Čas");
        odchod_1.getStyleClass().add("main-table-cell-center");
        odchod_1.setPrefWidth(65);
        TableColumn<MyData.OsobaDochazka, String> prichod_2 = new TableColumn<>("Čas");
        prichod_2.getStyleClass().add("main-table-cell-center");
        prichod_2.setPrefWidth(65);
        TableColumn<MyData.OsobaDochazka, String> odchod_2 = new TableColumn<>("Čas");
        odchod_2.getStyleClass().add("main-table-cell-center");
        odchod_2.setPrefWidth(65);
        TableColumn<MyData.OsobaDochazka, String> prichod_3 = new TableColumn<>("Čas");
        prichod_3.getStyleClass().add("main-table-cell-center");
        prichod_3.setPrefWidth(65);
        TableColumn<MyData.OsobaDochazka, String> odchod_3 = new TableColumn<>("Čas");
        odchod_3.getStyleClass().add("main-table-cell-center");
        odchod_3.getStyleClass().add("main-table-cell-last");
        odchod_3.setPrefWidth(65);

        colOrder.setCellFactory(column -> new TableCell<MyData.OsobaDochazka, Void>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); } else { setText(String.valueOf(getIndex() + 1)); }}}
        );
        jmenoUp.setCellValueFactory(new PropertyValueFactory<>("jmenoUp"));
        prichod_1.setCellValueFactory(new PropertyValueFactory<>("prichod_1"));
        odchod_1.setCellValueFactory(new PropertyValueFactory<>("odchod_1"));
        prichod_2.setCellValueFactory(new PropertyValueFactory<>("prichod_2"));
        odchod_2.setCellValueFactory(new PropertyValueFactory<>("odchod_2"));
        prichod_3.setCellValueFactory(new PropertyValueFactory<>("prichod_3"));
        odchod_3.setCellValueFactory(new PropertyValueFactory<>("odchod_3"));

        table.getColumns().addAll(colOrder, jmenoUp, prichod_1, odchod_1, prichod_2, odchod_2, prichod_3, odchod_3);

        ObservableList<MyData.OsobaDochazka> observableData = FXCollections.observableArrayList(osobySeznam);
        table.setItems(observableData);

        table.setSelectionModel(null);
        for (TableColumn<?, ?> column : table.getColumns()) { column.setSortable(false); }
        table.setSelectionModel(null);
        table.fixedCellSizeProperty().set(26.0);
        table.prefHeightProperty().bind(Bindings.size(observableData).multiply(table.fixedCellSizeProperty()));

        Region zapati = new Region();
        zapati.setPrefHeight(8);
        HBox.setHgrow(zapati, Priority.ALWAYS);

        tableBox.getChildren().add(zahlavy);
        tableBox.getChildren().add(table);
        tableBox.getChildren().add(zapati);

        routesContainer.getChildren().add(tableBox);

        if (oddeleni.equals("SKLAD") && mojeAplikacniData.getPoznamkaSkladu() != null) {
            if (!mojeAplikacniData.getPoznamkaSkladu().isEmpty()) {
                VBox allNote = new VBox();
                allNote.setFillWidth(true);
                allNote.getStyleClass().add("main-table-box");

                Label textNoteZahlavi = new Label();
                textNoteZahlavi.setAlignment(Pos.CENTER_LEFT);
                textNoteZahlavi.getStyleClass().add("main-table-box-label-left");
                textNoteZahlavi.setPrefWidth(185);
                textNoteZahlavi.setText("POZNÁMKA PRO SKLAD");
                allNote.getChildren().addAll(textNoteZahlavi);

                HBox boxNote = new HBox();
                HBox.setHgrow(boxNote, Priority.ALWAYS);
                boxNote.getStyleClass().add("poznamka-box");
                boxNote.setMaxWidth(Double.MAX_VALUE);

                Label textNote = new Label();
                textNote.getStyleClass().add("poznamka");
                textNote.setText(mojeAplikacniData.getPoznamkaSkladu());
                textNote.setMaxWidth(570);
                textNote.setWrapText(true);
                boxNote.getChildren().add(textNote);
                allNote.getChildren().add(boxNote);

                Region zapatiNote = new Region();
                zapatiNote.setPrefHeight(8);
                HBox.setHgrow(zapatiNote, Priority.ALWAYS);
                allNote.getChildren().add(zapatiNote);

                routesContainer.getChildren().add(allNote);
            }
        }
    }

    public void setProgressBar(boolean visibleBox, boolean visibleBar, String text){
        mainProgressBox.setVisible(visibleBox);
        mainProgressBox.setManaged(visibleBox);
        mainProgress.setVisible(visibleBar);
        mainProgress.setManaged(visibleBar);
        mainProgressText.setVisible(visibleBox);
        mainProgressText.setManaged(visibleBox);
        mainProgressText.setText(text);
        routesContainerScrool.setVisible(!visibleBox);
        routesContainerScrool.setManaged(!visibleBox);
    }

    private String writeTrasy(int showMod){

        if (nacteneTrasy == null || nacteneTrasy.isEmpty()) return "";

        int idTrasy = 0;
        List<String> infoWindow = new ArrayList<>();
        List<String> osoby = new ArrayList<>();

        for (MyRoute trasa: nacteneTrasy) {

            if (trasa.getDump().isEmpty()) continue;

            trasa.setPosadka(trasa.spz); trasa.setTelefon("");
            boolean nalezenaPos = false;
            /*for (MyData.RidicZavoznik r: mojeAplikacniData.getZavoznici()) {
                if (trasa.spz.contains(r.getJmenoUp())) {
                    trasa.posadka = r.getJmenoUp();
                    trasa.telefon = r.getTelefon();
                    nalezenaPos = true;
                    osoby.add(r.getJmenoUp());
                    break;
                }
            }*/

            for (MyData.Posadka p : mojeAplikacniData.getPosadka()){
                if (trasa.spz.toUpperCase().equals(p.getSpz().toUpperCase())){
                    if (p.getRidic() != null){
                        trasa.posadka = "";
                        if (p.getAuto().toUpperCase().contains("BOXER")) trasa.posadka = "BOXER ";
                        trasa.posadka += p.getRidic().getJmenoUp();
                        trasa.telefon = p.getRidic().getTelefon();
                        osoby.add(p.getRidic().getJmenoUp());
                        if (p.getZavoznik() != null) {
                            trasa.posadka = trasa.posadka + " + " + p.getZavoznik().getJmenoUp();
                            osoby.add(p.getZavoznik().getJmenoUp());
                        }
                        nalezenaPos = true;
                        break;
                    }
                }
            }

            idTrasy ++;
            if (showMod == 1 && !nalezenaPos) infoWindow.add("U trasy " + idTrasy + " nepřiřazena posádka.");
            if (showMod == 2 && trasa.telefon.isEmpty()) infoWindow.add("U trasy " + idTrasy + " není zadán telefon na řidiče.");

            if (showMod == 3) if (!trasa.factory.equals("1")) continue;
            //if (showMod == 2) if (!trasa.factory.equals("13")) continue;

            VBox tableBox = new VBox();
            tableBox.getStyleClass().add("main-table-box");

            Label zahlavy = new Label();
            zahlavy.setText(idTrasy + ".  " +  trasa.posadka + "   |   NAKLÁDKA " + trasa.startTime);
            zahlavy.getStyleClass().add("main-table-box-driver-label-left");

            TableView<MyRouteDump> table = new TableView<>() {{
                setFocusTraversable(false);
                addEventFilter(ScrollEvent.SCROLL, event -> {
                    event.consume();
                    if (getParent() != null) {
                        getParent().fireEvent(event);
                    }
                });
            }};
            table.getStyleClass().add("main-table");

            TableColumn<MyRouteDump, String> colOrder = new TableColumn<>("Číslo");
            colOrder.getStyleClass().add("main-table-cell-right");
            colOrder.setPrefWidth(30);
            TableColumn<MyRouteDump, String> colName = new TableColumn<>("Jméno");
            colName.getStyleClass().add("main-table-cell-left");
            colName.setPrefWidth(245);
            TableColumn<MyRouteDump, String> colCity = new TableColumn<>("Město");
            colCity.getStyleClass().add("main-table-cell-left");
            colCity.setPrefWidth(230);
            TableColumn<MyRouteDump, String> colTime = new TableColumn<>("Čas");
            colTime.getStyleClass().add("main-table-cell-last");
            colTime.setPrefWidth(65);

            colOrder.setCellValueFactory(new PropertyValueFactory<>("order"));
            colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
            colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

            table.getColumns().addAll(colOrder, colName, colCity, colTime);

            ObservableList<MyRouteDump> observableData = FXCollections.observableArrayList(trasa.dump);
            table.setItems(observableData);

            table.setSelectionModel(null);
            for (TableColumn<?, ?> column : table.getColumns()) { column.setSortable(false); }
            table.setSelectionModel(null);
            table.fixedCellSizeProperty().set(26.0);
            table.prefHeightProperty().bind(Bindings.size(observableData).multiply(table.fixedCellSizeProperty()));

            HBox zapati = new HBox();

            if (trasa.factory.equals("1")) {
                Label text1 = new Label();
                text1.getStyleClass().add("main-table-box-label-left");
                text1.setText("POZNÁMKA: " + trasa.note);

                Label text3 = new Label();
                text3.setText("TRASA " + trasa.km);

                Label text31 = new Label();
                text31.getStyleClass().add("main-table-box-label-right");
                text31.setText("km");

                Region mezera1 = new Region();

                HBox.setHgrow(mezera1, Priority.ALWAYS);

                zapati.getChildren().addAll(text1, mezera1, text3, text31);
            }
            else if (showMod == 1) {
                Label text1 = new Label();
                text1.getStyleClass().add("main-table-box-label-left");
                text1.setText("KONEC " + trasa.endTime);

                Label text2 = new Label();
                text2.setText("HMOTNOST " + trasa.tonne);

                Label text21 = new Label();
                text21.getStyleClass().add("main-table-box-label-right");
                text21.setText("t");

                Label text3 = new Label();
                text3.setText("TRASA " + trasa.km);

                Label text31 = new Label();
                text31.getStyleClass().add("main-table-box-label-right");
                text31.setText("km");

                Region mezera1 = new Region();
                Region mezera2 = new Region();

                HBox.setHgrow(mezera1, Priority.ALWAYS);
                HBox.setHgrow(mezera2, Priority.ALWAYS);

                zapati.getChildren().addAll(text1, mezera1, text2, text21, mezera2, text3, text31);
            }
            else{
                Label text1 = new Label();
                text1.getStyleClass().add("main-table-box-label-left");
                text1.setText("TEL: " + trasa.telefon);

                zapati.getChildren().add(text1);
            }

            tableBox.getChildren().add(zahlavy);
            tableBox.getChildren().add(table);
            tableBox.getChildren().add(zapati);

            routesContainer.getChildren().add(tableBox);
        }

        if (showMod == 1 && nacteneTrasy.size() > 0) {

            List<Integer> prumerVahaHodnoty = new ArrayList<>();
            int celkemTras = 0;
            int celkemKm = 0;
            int maxKm = 0, minKm = 5000;
            double celkemTun = 0;
            int celkemZastavek = 0;

            for (MyRoute trasa: nacteneTrasy){
                int km = 0;
                if (trasa.dump.size() > 0) celkemTras++;
                try {
                    km = Integer.parseInt(trasa.km.replace(",", "."));
                    if (km > maxKm) maxKm = km;
                    if (km < minKm) minKm = km;
                } catch (Exception e) {
                    km = 0;
                }
                celkemKm += km;
                double tn = 0;
                try {
                    tn = Double.parseDouble(trasa.tonne.replace(",", "."));
                } catch (Exception e) {
                    tn = 0;
                }
                celkemTun += tn;
                celkemZastavek += trasa.dump.size();

                for (MyRouteDump z: trasa.dump){
                    Integer kg = 0;
                    try {
                        kg = Integer.parseInt(z.weight.replace(",", "."));
                    } catch (Exception e) {
                        kg = 0;
                    }
                    if (kg > 0) prumerVahaHodnoty.add(kg);
                }
            }
            if (minKm == 5000) minKm = 0;
            double prumerVaha = prumerVahaHodnoty.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(0.0); // Vrátí 0.0, pokud je seznam prázdný
            int prumerVahaKg = (int) Math.round(prumerVaha);

            record PrehledDat(String nazev, String hodnota) {}
            List<PrehledDat> prehledDat = new ArrayList<>();

            prehledDat.add(new PrehledDat("POČET TRAS", Integer.toString(celkemTras)));
            prehledDat.add(new PrehledDat("POČET ZASTÁVEK", Integer.toString(celkemZastavek)));
            prehledDat.add(new PrehledDat("CELKEM KM", Integer.toString(celkemKm) + " km"));
            prehledDat.add(new PrehledDat("NEJDELŠÍ TRASA", Integer.toString(maxKm) + " km"));
            prehledDat.add(new PrehledDat("NEJKRATŠÍ TRASA", Integer.toString(minKm) + " km"));
            prehledDat.add(new PrehledDat("CELKEM TUN", String.format(java.util.Locale.US, "%.1f", celkemTun).replace(".", ",") + " t"));
            prehledDat.add(new PrehledDat("PRŮMĚRNÁ VÁHA OBJEDNÁVKY", String.valueOf(prumerVahaKg) + " kg"));

            VBox allNote = new VBox();
            allNote.setFillWidth(true);
            allNote.getStyleClass().add("main-table-box");

            Label textNoteZahlavi = new Label();
            textNoteZahlavi.setAlignment(Pos.CENTER_LEFT);
            textNoteZahlavi.getStyleClass().add("main-table-box-label-left");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate datum = LocalDate.parse(nacteneTrasy.get(0).date, formatter);
            DateTimeFormatter vystupniFormatter = DateTimeFormatter.ofPattern("eeee dd.MM.yyyy", new Locale("cs", "CZ"));
            textNoteZahlavi.setText("PŘEHLED PRO DEN " + datum.format(vystupniFormatter).toUpperCase());
            allNote.getChildren().add(textNoteZahlavi);

            TableView<PrehledDat> table = new TableView<>() {{
                setFocusTraversable(false);
                addEventFilter(ScrollEvent.SCROLL, event -> {
                    event.consume();
                    if (getParent() != null) {
                        getParent().fireEvent(event);
                    }
                });
            }};
            table.getStyleClass().add("main-table");

            TableColumn<PrehledDat, String> nazev = new TableColumn<>("Název");
            nazev.getStyleClass().add("main-table-cell-left-oz");
            nazev.setPrefWidth(475);
            nazev.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nazev()));

            TableColumn<PrehledDat, String> hodnota = new TableColumn<>("Hodnota");
            hodnota.getStyleClass().add("main-table-cell-right-oz");
            hodnota.setPrefWidth(100);
            hodnota.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().hodnota()));

            table.getColumns().setAll(nazev, hodnota);

            ObservableList<PrehledDat> observableData = FXCollections.observableArrayList(prehledDat);
            table.setItems(observableData);

            for (TableColumn<?, ?> column : table.getColumns()) { column.setSortable(false); }
            table.setSelectionModel(null);
            table.fixedCellSizeProperty().set(26.0);
            table.prefHeightProperty().bind(Bindings.size(observableData).multiply(table.fixedCellSizeProperty()));

            allNote.getChildren().addAll(table);

            Region zapatiNote = new Region();
            zapatiNote.setPrefHeight(8);
            HBox.setHgrow(zapatiNote, Priority.ALWAYS);
            allNote.getChildren().add(zapatiNote);

            routesContainer.getChildren().add(allNote);
        }

        Set<String> jedinecne = new HashSet<>();
        Set<String> duplicitniOsoby = new HashSet<>();

        for (String osoba : osoby) {
            if (!jedinecne.add(osoba)) {
                duplicitniOsoby.add(osoba);
            }
        }

        if (showMod == 1 && !duplicitniOsoby.isEmpty()) {
            for (String jmeno : duplicitniOsoby) {
                infoWindow.add("Osoba jménem " + jmeno + " se vyskytuje na více trasách.");
            }
        }

        String hlaska = "";
        if (infoWindow.size() > 0){
            hlaska = String.join("\n", infoWindow);
        }

     //   RouteManager.ulozitTrasy(nacteneTrasy);

        return hlaska;
    }

    private void writeAll(int showMod){
        if (cekejteProsim){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
            return;
        }

        setProgressBar(true, true, "GENERUJI TABULKY");
        routesContainer.getChildren().clear();

        distribuceDenTextAktualizovat();

        if (showMod == 1 || showMod == 3) {
            if (showMod == 1 && (nacteneTrasy != null && nacteneTrasy.isEmpty()) && (mojeAplikacniData.getOsobySklad() != null && mojeAplikacniData.getOsobySklad().isEmpty())){
                setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
                Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                DialogManager.showConfirmDialog(aktuálníStage, "Nejprve načtěte nějaká data.", true, false, false);
                return;
            }

            if (showMod == 3){
                boolean isTrasy = false;
                if (nacteneTrasy != null && !nacteneTrasy.isEmpty()) for (MyRoute t: nacteneTrasy) if (t.factory.equals("1")) {isTrasy = true; break;}
                if (!isTrasy){
                    setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
                    Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                    DialogManager.showConfirmDialog(aktuálníStage, "Nejprve vytvořte nějakou trasu.", true, false, false);
                    return;
                }
            }
            else writeDochazka(mojeAplikacniData.getOsobySklad());

            String hlaska = writeTrasy(showMod);
            setProgressBar(false, false, "");
            double scrollHeight = 620;
            if (routesContainer.getScene().getHeight() - 180 > scrollHeight) scrollHeight = routesContainer.getScene().getHeight() - 180;
            routesContainerScrool.setMaxHeight(scrollHeight);
            if (!hlaska.isEmpty() && showMod == 1){
                Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                DialogManager.showConfirmDialog(aktuálníStage, hlaska, true, false, false);
            }
            hlavniTabulkaMod = showMod;
        }
        if (showMod == 2) {
            setProgressBar(true, true, "Načítám data z dostupného zdroje...");
            Thread thread = new Thread(() -> {
                cekejteProsim = true;
                String myDate = "";
                if (nacteneTrasy != null && !nacteneTrasy.isEmpty() && nacteneTrasy.size() > 0) myDate = nacteneTrasy.get(0).date;
                String finalMyDate = importujDataObchodXml(myDate);
                Platform.runLater(() -> {
                    setProgressBar(false, false, "");
                    writeObchod(dataOz, finalMyDate);
                    writeObchod(dataOblast, finalMyDate);
                    writeDochazka(mojeAplikacniData.getOsobyDistribuce());
                    String hlaska = writeTrasy(showMod);
                    setProgressBar(false, false, "");
                    double scrollHeight = 620;
                    if (routesContainer.getScene().getHeight() - 180 > scrollHeight)
                        scrollHeight = routesContainer.getScene().getHeight() - 180;
                    routesContainerScrool.setMaxHeight(scrollHeight);
                    if (!hlaska.isEmpty()){
                        Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                        DialogManager.showConfirmDialog(aktuálníStage, hlaska, true, false, false);
                    }
                    hlavniTabulkaMod = showMod;
                    cekejteProsim = false;
                });
            });
            thread.setDaemon(true);
            thread.start(); // Spustí pozadí
        }
        if (showMod == 4) {
            if ((mojeAplikacniData.getOsobyDistribuce() != null && mojeAplikacniData.getOsobyDistribuce().isEmpty()) && (mojeAplikacniData.getOsobySklad() != null && mojeAplikacniData.getOsobySklad().isEmpty())){
                setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
                Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                DialogManager.showConfirmDialog(aktuálníStage, "Nejprve zadejte data do docházky.", true, false, false);
                return;
            }
            writeDochazka(mojeAplikacniData.getOsobySklad());
            writeDochazka(mojeAplikacniData.getOsobyDistribuce());
            setProgressBar(false, false, "");
            double scrollHeight = 620;
            if (routesContainer.getScene().getHeight() - 180 > scrollHeight) scrollHeight = routesContainer.getScene().getHeight() - 180;
            routesContainerScrool.setMaxHeight(scrollHeight);
            hlavniTabulkaMod = showMod;
        }
    }

    private boolean distribuceDenTextAktualizovat(){
        if (nacteneTrasy != null) {
            if (nacteneTrasy.size() > 0) {
                distribuceDenText.setText("Plán distribuce na " + formatujDatumDny(nacteneTrasy.get(0).date));
                return true;
            }
        }
        distribuceDenText.setText("Načtěte soubor .csv");
        return false;
    }

    private String formatujDatumDny(String datumText) {
        if (datumText == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate datum = LocalDate.parse(datumText, formatter);
        String[] dnyVeCtvrtemPade = { "", "pondělí", "úterý", "středu", "čtvrtek", "pátek", "sobotu", "neděli" };

        int denVTydnu = datum.getDayOfWeek().getValue();
        String denNazev = dnyVeCtvrtemPade[denVTydnu];

        return denNazev + " " + datumText;
    }

    @FXML
    protected void selectCsvFile(ActionEvent event) {
        if (cekejteProsim){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
            return;
        }

        Button tlacitko = (Button) event.getSource();
        boolean addCsv = tlacitko.getId().equals("csvButton") ? false : true;

        if (addCsv && nacteneTrasy.isEmpty()){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Nejprve načtěte hlavní .csv", true, false, false);
            return;
        }

        distribuceDenText.setText("Načtěte soubor .csv");
        setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
        routesContainer.getChildren().clear();

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("CSV soubory (*.csv)", "*.csv");
        fileChooser.getExtensionFilters().add(filter);

        String userHome = System.getProperty("user.home");
        File desktopDir = new File(userHome, "OneDrive" + File.separator + "Plocha");

        if (!desktopDir.exists()) {
            desktopDir = new File(userHome, "OneDrive" + File.separator + "Desktop");
        }
        if (!desktopDir.exists()) {
            desktopDir = new File(userHome, "Desktop");
        }
        if (!desktopDir.exists()) {
            desktopDir = new File(userHome, "Plocha");
        }
        if (desktopDir.exists() && desktopDir.isDirectory()) {
            fileChooser.setInitialDirectory(desktopDir);
        } else {
            fileChooser.setInitialDirectory(new File(userHome));
        }
        Stage aktualniOkno = (Stage) csvButton.getScene().getWindow();

        File vybranySoubor = fileChooser.showOpenDialog(aktualniOkno);

        if (vybranySoubor != null) {
            try {
                String souborCsv = Files.readString(vybranySoubor.toPath(), StandardCharsets.UTF_8);
              //  nacteneTrasy = naplnTrasyZCsv(souborCsv, addCsv);
                RouteManager.get().setTrasy(naplnTrasyZCsv(souborCsv, addCsv));
                if (nacteneTrasy == null || nacteneTrasy.isEmpty()) {
                    setProgressBar(true, false, "VYSKYTLA SE CHYBA, ZKUSTE TO ZNOVU.");
                }
                else writeAll(1);

            } catch (IOException e) {
                System.err.println("Chyba při čtení vybraného souboru: " + e.getMessage());
                distribuceDenTextAktualizovat();
                e.printStackTrace();
            }
        } else {
            System.out.println("Uživatel stornoval výběr souboru.");
            distribuceDenTextAktualizovat();
        }
        distribuceDenTextAktualizovat();
    }

    @FXML
    public void generovatVseKomplet(){
        if (tabulkaProSklad) writeAll(1);
        else writeAll(2);
    }

    @FXML
    public void generovatUzivTrasy(){
         writeAll(3);
    }

    @FXML
    public void generovatDochazku(){
        writeAll(4);
    }

    private ArrayList<MyRoute> naplnTrasyZCsv(String obsahCsv, boolean addCsv) {
        setProgressBar(true, true, "ZPRACOVÁVÁM DATA");
        ArrayList<MyRoute> vytvoreneTrasy = new ArrayList<>();

        if (nacteneTrasy != null) {
            for (MyRoute t : nacteneTrasy){
                if (addCsv) vytvoreneTrasy.add(t);
                else if (t.factory.equals("1")) vytvoreneTrasy.add(t);
            }
        }

        ArrayList<MyRoute> seznamTras = new ArrayList<>();

        String[] radky = obsahCsv.split("\\r?\\n");

        ArrayList<MyRouteDump> vsechnyZastavky = new ArrayList<>();

        for (String radek : radky) {
            if (radek.trim().isEmpty()) continue;
            String[] arr = radek.split(";", -1);
            if (!arr[0].equals("RIRO_OUTPUT_ITINERARY_V2.01")) {
                setProgressBar(true, false, "NEPLATNÝ FORMÁT SOUBORU");
                Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
                DialogManager.showConfirmDialog(aktuálníStage, "Neplatný formát souboru!", true, false, false);
                return null;
            }
            if (arr.length < 44) continue;
            /*    2    provoz       1    cislo trasy       22   poradi             25   nazev odberatele       29   mesto
                  26   adresa       37   cas skladka       43   objednavka kg      32   GPS                    33   GPS
                  4    spz         31   poznamka          16   cas start trasy    17   cas cil                3    datum
                  7    celkem km    12   celkem t  */

            MyRouteDump zastavka = new MyRouteDump(arr[22], arr[25], arr[26], arr[29], arr[37], arr[43], arr[33], arr[32],
                    arr[2], arr[1], arr[4], arr[31], arr[16], arr[17], arr[3], arr[7], arr[12]);
            vsechnyZastavky.add(zastavka);
        }

        for (MyRouteDump z : vsechnyZastavky) {

            MyRoute existujiciTrasa = null;
            for (MyRoute r : seznamTras) {
                if (r.number.equals(z.routeNumber)) {
                    existujiciTrasa = r;
                    break;
                }
            }

            if (existujiciTrasa == null) {
                existujiciTrasa = new MyRoute("", z.routeFactory, z.routeNumber, z.routeSpz, z.routeNote, z.routeStartTime, z.routeEndTime, z.routeDate, z.routeKm, z.routeTonne, "");
                seznamTras.add(existujiciTrasa);
            }

            boolean addZastavka = true;
            for (MyRouteDump e : existujiciTrasa.dump) {
                if (e.order.equals(z.order)) {
                    addZastavka = false;
                    break;
                }
            }
            if (addZastavka) existujiciTrasa.dump.add(z);
        }

        seznamTras.addAll(vytvoreneTrasy);
        seraditTrasy(seznamTras);

        LocationManager.ulozitLokace(seznamTras);

        return seznamTras;
    }

    private void seraditTrasy(List<MyRoute> seznamTras){
        seznamTras.sort((t1, t2) -> {
            String s1 = t1.getStartTime();
            String s2 = t2.getStartTime();
            boolean p1 = (s1 == null || s1.equalsIgnoreCase("DNES"));
            boolean p2 = (s2 == null || s2.equalsIgnoreCase("DNES"));
            if (p1 != p2) {
                return p1 ? -1 : 1;
            }
            int timeCompare = 0;
            if (s1 != null && s2 != null) {
                timeCompare = s1.compareTo(s2);
            }
            if (timeCompare == 0) {
                String f1 = t1.getFactory();
                String f2 = t2.getFactory();

                boolean f1JeJednicka = "1".equals(f1);
                boolean f2JeJednicka = "1".equals(f2);

                if (f1JeJednicka != f2JeJednicka) {
                    return f1JeJednicka ? -1 : 1;
                }
            }
            return timeCompare;
        });

        for (MyRoute r : seznamTras) {
            if (r.getDump() != null) {
                r.getDump().sort(Comparator.comparing(
                        MyRouteDump::getTime,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ));
            }
            int p = 1;
            for (MyRouteDump d : r.getDump()){
                d.setOrder(Integer.toString(p));
                p++;
            }
        }

        LocalDate zitra = LocalDate.now().plusDays(1);
        String myDate = zitra.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        for (MyRoute r: seznamTras) if (r.factory.equals("13")) { myDate = r.date; }
        for (MyRoute r: seznamTras) if (r.factory.equals("1")) { r.date = myDate; }
     //   RouteManager.get().setTrasy(seznamTras);
    }

    @FXML
    public void windowVytvoritTrasu() {
        if (cekejteProsim){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
            return;
        }
        setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
        routesContainer.getChildren().clear();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("my-trasy-view.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Správa vlastních tras");
            settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/plgdistribuce/ikona.png")));
            settingsStage.setScene(scene);
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setResizable(false);

            settingsStage.setOnCloseRequest(event -> {
                Object controller = fxmlLoader.getController();
                if (controller instanceof TrasyController) {
                    TrasyController ulozit = (TrasyController) controller;
                    ulozit.ulozitVsechnyUpravy();
                }
            });
            settingsStage.showAndWait();

         //   nacteneTrasy = RouteManager.nacistTrasy();
            seraditTrasy(nacteneTrasy);

            buttonUzivTrasyText();
            for (MyRoute r: nacteneTrasy){if (r.factory.equals("1")){ writeAll(3); break;}}
        //    RouteManager.ulozitTrasy(nacteneTrasy);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Chyba při otevírání okna nastavení!");
        }
    }

    @FXML
    public void windowZmenaDatum() {
        if (cekejteProsim){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
            return;
        }
        if (nacteneTrasy == null || nacteneTrasy.isEmpty() || nacteneTrasy.size() == 0){
            setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
            DialogManager.showConfirmDialog((Stage) routesContainer.getScene().getWindow(), "Žádné trasy ke změně datumu.", true, false, false);
            return;
        }

        setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
        routesContainer.getChildren().clear();
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("date-dialog.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage settingsStage = new Stage();
            settingsStage.setTitle("Změna datumu celého plánu");
            settingsStage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/plgdistribuce/ikona.png")));
            settingsStage.setScene(scene);
            settingsStage.initModality(Modality.APPLICATION_MODAL);
            settingsStage.setResizable(false);

            DispecinkController.ZmenaDatumDialogController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.btnOk.setOnAction(event -> {
                    String vybraneDatum = controller.getFormattedDate();
                    if (vybraneDatum == null) {
                        Stage hlavniOkno = (Stage) routesContainer.getScene().getWindow();
                        DialogManager.showConfirmDialog(hlavniOkno, "Vyberte datum pro změnu.", true, false, false);
                    }
                    else {
                        Stage hlavniOkno = (Stage) routesContainer.getScene().getWindow();
                        for (MyRoute r : nacteneTrasy) { r.date = vybraneDatum; }
                        distribuceDenTextAktualizovat();
                        settingsStage.close();
                        Platform.runLater(() -> {
                            String zprava = "Plán distribuce změněn na " + formatujDatumDny(nacteneTrasy.get(0).date) + ".";
                            DialogManager.showConfirmDialog(hlavniOkno, zprava, true, false, false);
                           // writeAll(1);
                        });
                    }
                });

                controller.btnStorno.setOnAction(event -> {
                    settingsStage.close();
                });
            }

            settingsStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Chyba při otevírání okna nastavení!");
        }
    }

    public static class ZmenaDatumDialogController {
        @FXML public Button btnOk, btnStorno;
        @FXML public DatePicker datePicker;
        public String getFormattedDate() {
            LocalDate date = datePicker.getValue();
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                return date.format(formatter);
            }
            return null;
        }
    }

    @FXML
    public void smazatVlastniTrasy(){
        if (cekejteProsim){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Čekejte prosím...", true, false, false);
            return;
        }
        boolean isRoute = false;
        for (MyRoute r: nacteneTrasy){if (r.factory.equals("1")){ isRoute = true; break;}}
        Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
        if (!isRoute){
            DialogManager.showConfirmDialog(aktuálníStage, "Nemáte žádné vlastní trasy.", true, false, false);
            return;
        }
        boolean potvrdil = DialogManager.showConfirmDialog(aktuálníStage, "Opravdu chcete smazat vlastní trasy?", false, true, true);
        if (!potvrdil) return;

        for (MyRoute t: nacteneTrasy){
            if (t.factory.equals("1")){
                for (MyRouteDump d: t.getDump()){
                    for (MyRoute r: nacteneTrasy) {
                        if (d.routeNumber.equals(r.number)) {
                            r.dump.add(d);
                        }
                    }
                }
            }
        }

        nacteneTrasy.removeIf(ii -> ii.factory.equals("1"));
        buttonUzivTrasyText();
       // RouteManager.ulozitTrasy(nacteneTrasy);
        setProgressBar(true, false, "PIVOVARY LOBKOWICZ");
        routesContainer.getChildren().clear();
    }

    private void buttonUzivTrasyText(){
        int uzivTrasa = 0;
        for (MyRoute r: nacteneTrasy) if (r.factory.equals("1")) uzivTrasa++;
        buttonUzivTrasy.setText("Generovat vytvořené trasy (" + Integer.toString(uzivTrasa) + ")");
    }

    public void odeslatEmailTest(){

        if (routesContainer.getChildren().size() == 0) {
            JOptionPane.showMessageDialog(null, "generuj.");
            return;
        }

        String predmetText;
        if (nacteneTrasy.size() > 0) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate datum = LocalDate.parse(nacteneTrasy.get(0).date, formatter);
            DateTimeFormatter vystupniFormatter = DateTimeFormatter.ofPattern("eeee dd.MM.yyyy", new Locale("cs", "CZ"));
            if (tabulkaProSklad) predmetText = "Plán práce na " + datum.format(vystupniFormatter);
            else predmetText = "Plán rozvozu na " + datum.format(vystupniFormatter);
        } else {
            predmetText = "NEZNAMO";
        }

        String hlavni = "";
        String ostatni = "";

        boolean prvni = true;
        for (MyData.EmailKontakt e: mojeAplikacniData.getEmaily()) {
            if (prvni) {if ((tabulkaProSklad && e.isEmailDistribuce()) || (!tabulkaProSklad && e.isEmailObchod())) {hlavni = e.getEmail(); prvni = false;}}
            else {if ((tabulkaProSklad && e.isEmailDistribuce()) || (!tabulkaProSklad && e.isEmailObchod())) ostatni += e.getEmail() + ";";}
        }

        String htmlTabulka = vygenerujHtml1();
        String finalHlavni = hlavni;
        String finalOstatni = ostatni;
        new Thread(() -> {
            OutlookAutomation.otevrTrasyVOutlooku(
                    finalHlavni,
                    finalOstatni,
                    predmetText,
                    htmlTabulka
            );
        }).start();
    }

    public void odeslatEmailDochazka(){

        if (!(routesContainer.getChildren().size() > 0 && hlavniTabulkaMod == 4)) {
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Nejprve generujte tabulku docházky.", true, false, false);
            return;
        }

        LocalDate datum;
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern("eeee dd.MM.yyyy")
                .toFormatter(new Locale("cs", "CZ"));

        try {
            datum = LocalDate.parse(denDochazkyButton.getText(), formatter);
        } catch (DateTimeParseException e) {
            datum = LocalDate.now().plusDays(1);
        }

        DayOfWeek denVTydnu = datum.getDayOfWeek();

        if (denVTydnu == DayOfWeek.SATURDAY || denVTydnu == DayOfWeek.SUNDAY){
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            boolean potvrdil = DialogManager.showConfirmDialog(aktuálníStage, "Opravdu chcete odeslat plán na víkend?", false, true, true);
            if (!potvrdil) return;
        }

        DateTimeFormatter form = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String naformatovaneDatum = datum.format(form);
        CsvDataExporter.RouteWorkDataExport(nacteneTrasy, mojeAplikacniData, naformatovaneDatum, false);

        String predmetText;
        predmetText = "Plán práce na " + formatujDatumDny(naformatovaneDatum);

        String hlavni = "";
        String ostatni = "";

        boolean prvni = true;
        for (MyData.EmailKontakt e: mojeAplikacniData.getEmaily()) {
            if (prvni) {if (tabulkaProSklad && e.isEmailDistribuce()) {hlavni = e.getEmail(); prvni = false;}}
            else {if (tabulkaProSklad && e.isEmailDistribuce()) ostatni += e.getEmail() + ";";}
        }

        String htmlTabulka = vygenerujHtml(true);
        String finalHlavni = hlavni;
        String finalOstatni = ostatni;
        new Thread(() -> {
            OutlookAutomation.otevrTrasyVOutlooku(
                    finalHlavni,
                    finalOstatni,
                    predmetText,
                    htmlTabulka
            );
        }).start();
    }

    public void odeslatEmailHlavni(){

        if (!(routesContainer.getChildren().size() > 0 && (hlavniTabulkaMod == 1 || hlavniTabulkaMod == 2))) {
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Nejprve generujte hlavní tabulku.", true, false, false);
            return;
        }
/*
        if (nacteneTrasy == null || nacteneTrasy.isEmpty()) {
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            DialogManager.showConfirmDialog(aktuálníStage, "Nejprve načtěte nějaké trasy.", true, false, false);
            return;
        }*/

        String predmetText;
        if (nacteneTrasy != null && !nacteneTrasy.isEmpty()) {
            if (nacteneTrasy.size() > 0) {
                if (tabulkaProSklad) {
                    predmetText = "Plán práce na " + formatujDatumDny(nacteneTrasy.get(0).date);
                    CsvDataExporter.RouteWorkDataExport(nacteneTrasy, mojeAplikacniData, nacteneTrasy.get(0).date, true);
                }
                else predmetText = "Plán distribuce na " + formatujDatumDny(nacteneTrasy.get(0).date);
            } else {
                predmetText = "";
            }
        }
        else {
            Stage aktuálníStage = (Stage) routesContainer.getScene().getWindow();
            boolean potvrdil = DialogManager.showConfirmDialog(aktuálníStage, "Opravdu chcete odeslat plán bez načtených tras?", false, true, true);
            if (potvrdil) {
                LocalDate datum;
                datum = LocalDate.now().plusDays(1);
                DateTimeFormatter infoDate = DateTimeFormatter.ofPattern("eeee dd.MM.yyyy", new Locale("cs", "CZ"));
                predmetText = "Plán na " + datum.format(infoDate);
            }
            else return;
        }

        String hlavni = "";
        String ostatni = "";

        boolean prvni = true;
        for (MyData.EmailKontakt e: mojeAplikacniData.getEmaily()) {
            if (prvni) {if ((tabulkaProSklad && e.isEmailDistribuce()) || (!tabulkaProSklad && e.isEmailObchod())) {hlavni = e.getEmail(); prvni = false;}}
            else {if ((tabulkaProSklad && e.isEmailDistribuce()) || (!tabulkaProSklad && e.isEmailObchod())) ostatni += e.getEmail() + ";";}
            }

        String htmlTabulka = vygenerujHtml(false);
        String finalHlavni = hlavni;
        String finalOstatni = ostatni;
        new Thread(() -> {
            OutlookAutomation.otevrTrasyVOutlooku(
                    finalHlavni,
                    finalOstatni,
                    predmetText,
                    htmlTabulka
            );
        }).start();
    }

    public String vygenerujHtml(boolean pouzeDochazka) {
        if (routesContainer.getChildren().size() == 0) {
            return "<div style=\"font-family:Arial,sans-serif;\">Není nic k odeslání.</div>";
        }

        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<style>");
        html.append("  a:link, span.MsoHyperlink, a:visited, span.MsoHyperlinkFollowed {");
        html.append("    color: #ffffff !important;");
        html.append("    text-decoration: underline !important;");
        html.append("  }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body style='font-family:Arial,sans-serif; padding: 0; margin: 0;'>");
       // html.append("<body style='font-family:Arial,sans-serif; margin: 0; padding: 0; -webkit-text-size-adjust: 100%;'>");
        html.append("<table cellpadding='0' cellspacing='0' style='width:560px; min-width:200px; max-width:560px; border-collapse: separate; overflow: hidden; padding: 0; margin: 0;'>");
        html.append("<tr>");
        html.append("<td style='width:560px; min-width:200px; max-width:560px; padding: 0; margin: 0;'>");

        if (pouzeDochazka){
            htmlDochazka(html, mojeAplikacniData.getOsobySklad());
            htmlDochazka(html, mojeAplikacniData.getOsobyDistribuce());
        }
        else {
            if (!tabulkaProSklad) {
                htmlObchod(html, dataOz);
                htmlObchod(html, dataOblast);
            }

            if (tabulkaProSklad) htmlDochazka(html, mojeAplikacniData.getOsobySklad());
            else htmlDochazka(html, mojeAplikacniData.getOsobyDistribuce());
            if (tabulkaProSklad) htmlTrasy(html, mojeAplikacniData.getOsobySklad());
            else htmlTrasy(html, mojeAplikacniData.getOsobyDistribuce());
        }

        html.append("</td>");
        html.append("</tr>");
        html.append("</table>");
        html.append("</body>");

       // ulozDoHtml(html.toString(), "test.htm");

        return html.toString();
    }

    public String vygenerujHtml1() {

        StringBuilder html = new StringBuilder();

        try {
            // Načte celý soubor test.htm z kořenového adresáře projektu v kódování UTF-8
            String test = Files.readString(Paths.get("test.htm"));
            html.append(test);
        } catch (IOException e) {
            System.err.println("Chyba při čtení souboru: " + e.getMessage());
            e.printStackTrace();
        }

        return html.toString();
    }

    private StringBuilder htmlObchod(StringBuilder html, List<ImportDataObchod> dataObchod){

        if (dataObchod.size() == 0) return html;

        String dataObOz = dataObchod == dataOz ? "PŘEHLED DLE OZ" : "PŘEHLED DLE OBCH. OBLASTÍ";

        html.append("<div style='border:1px solid #ffffff; border-radius:10px; overflow:hidden;'>");
        // 0. TABULKA
        html.append("<table cellpadding='0' cellspacing='0' width='100%' style='width:100%; border-collapse: collapse; margin: 0; padding: 0;'>");
        // 1. ŘÁDEK - MODRÁ HLAVIČKA
        html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");

        html.append("<td width='400' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:400px; min-width:200px; max-width:400px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("<p style='font-size:14px; line-height:14px; text-align:left; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
        html.append("&nbsp;&nbsp;&nbsp;" + dataObOz);
        html.append("</p>");
        html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td>");
        html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; min-width:100px; max-width:160px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("<p style='font-size:14px; line-height:14px; text-align:right; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
        html.append("OBJEM HL&nbsp;&nbsp;");
        html.append("</p>");
        html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td>");
        html.append("</tr>");

        // 2. ŘÁDKY OSOBY
        for (int o = 0; o < dataObchod.size(); o++) {
            ImportDataObchod dataOb = dataObchod.get(o);

            if (o % 2 == 0) html.append("<tr style='background-color: #FFFFFF;'>");
            else html.append("<tr style='background-color: #F8FAFC;'>");

            html.append("<td width='400' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:400px; min-width:200px; max-width:400px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #1e1d1d; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:12px; line-height:12px; text-align:left; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;&nbsp;" + dataOb.getNazev());
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:100px; max-width:160px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:12px; line-height:12px; text-align:right; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;" + dataOb.getHl());
            html.append("<span style='font-size: 7px;'> hl&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            html.append("</tr>");

        }

        // 3. ŘÁDEK - SUMÁŘ
        html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");
        html.append("<td colspan='2' style='white-space:nowrap; -webkit-text-size-adjust:none !important; min-width:200px; max-width:560px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td></tr></table>");
        html.append("</div>"); // Uzavření obalového DIVu trasy

        html.append("<p style='font-size:14px; line-height:14px; mso-line-height-rule:exactly; margin:0; padding: 0;'>&nbsp;</p>");

        return html;
    }

    private StringBuilder htmlDochazka(StringBuilder html, List<MyData.OsobaDochazka> osobySkladDistribuce){
        List<MyData.OsobaDochazka> osobySeznam = new ArrayList<>();

        for (MyData.OsobaDochazka o: osobySkladDistribuce){
            if (!o.getJmenoUp().isEmpty()) osobySeznam.add(o);
        }

        if (osobySeznam.size() == 0) return html;

        String oddeleni = osobySkladDistribuce == mojeAplikacniData.getOsobySklad() ? "SKLAD" : "DISTRIBUCE";

        html.append("<div style='border:1px solid #ffffff; border-radius:10px; overflow:hidden;'>");
        // 0. TABULKA
        html.append("<table cellpadding='0' cellspacing='0' width='100%' style='width:100%; border-collapse: collapse; margin: 0; padding: 0;'>");
        // 1. ŘÁDEK - MODRÁ HLAVIČKA
        html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");

        html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; min-width:100px; max-width:290px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("<p style='font-size:14px; line-height:14px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
        html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + oddeleni);
        html.append("</p>");
        html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td>");
        String text = "";
        if (oddeleni.equals("SKLAD") && mojeAplikacniData.isRezimSkladu()) text = "NOC";
        else text = "RÁNO";
        html.append("<td colspan='2' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:90px; max-width:130px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("<p style='font-size:14px; text-align:center; line-height:14px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
        html.append(text);
        html.append("</p>");
        html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td>");
        if (oddeleni.equals("SKLAD") && mojeAplikacniData.isRezimSkladu()) text = "RÁNO";
        else text = "ODPOLEDNE";
        html.append("<td colspan='2' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:90px; max-width:130px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("<p style='font-size:14px; text-align:center; line-height:14px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
        html.append(text);
        html.append("</p>");
        html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td>");
        if (oddeleni.equals("SKLAD") && mojeAplikacniData.isRezimSkladu()) text = "ODPOLEDNE";
        else text = "NOC";
        html.append("<td colspan='2' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:90px; max-width:130px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("<p style='font-size:14px; text-align:center; line-height:14px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
        html.append(text);
        html.append("</p>");
        html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td>");
        html.append("</tr>");

        // 2. ŘÁDKY OSOBY
        for (int o = 0; o < osobySeznam.size(); o++) {
            MyData.OsobaDochazka osoba = osobySeznam.get(o);

            if (o % 2 == 0) html.append("<tr style='background-color: #FFFFFF;'>");
            else html.append("<tr style='background-color: #F8FAFC;'>");

            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:110px; max-width:280px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #1e1d1d; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:12px; line-height:12px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;&nbsp;" + osoba.getJmenoUp() + "&nbsp;&nbsp;");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            String height = testCas(osoba.getPrichod_1());
            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:45px; max-width:65px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:"+height+"px; line-height:12px; text-align:center; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;" + osoba.getPrichod_1() + "&nbsp;");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            height = testCas(osoba.getOdchod_1());
            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:45px; max-width:65px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:"+height+"px; line-height:12px; text-align:center; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;" + osoba.getOdchod_1() + "&nbsp;");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            height = testCas(osoba.getPrichod_2());
            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:45px; max-width:65px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:"+height+"px; line-height:12px; text-align:center; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;" + osoba.getPrichod_2() + "&nbsp;");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            height = testCas(osoba.getOdchod_2());
            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:45px; max-width:65px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:"+height+"px; line-height:12px; text-align:center; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;" + osoba.getOdchod_2() + "&nbsp;");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            height = testCas(osoba.getPrichod_3());
            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:45px; max-width:65px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:"+height+"px; line-height:12px; text-align:center; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;" + osoba.getPrichod_3() + "&nbsp;");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            height = testCas(osoba.getOdchod_3());
            html.append("<td style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:45px; max-width:65px; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:"+height+"px; line-height:12px; text-align:center; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;" + osoba.getOdchod_3() + "&nbsp;");
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td>");

            html.append("</tr>");

        }

        // 3. ŘÁDEK - SUMÁŘ
        html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");
        html.append("<td colspan='7' style='min-width:200px; max-width:560px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
        html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
        html.append("</td></tr></table>");
        html.append("</div>"); // Uzavření obalového DIVu trasy

        html.append("<p style='font-size:14px; line-height:14px; mso-line-height-rule:exactly; margin:0; padding: 0;'>&nbsp;</p>");

        if (mojeAplikacniData.getPoznamkaSkladu() != null) {
            if (oddeleni.equals("SKLAD") && !mojeAplikacniData.getPoznamkaSkladu().isEmpty()) {
                html.append("<div style='border:1px solid #ffffff; border-radius:10px; overflow:hidden;'>");
                html.append("<table cellpadding='0' cellspacing='0' width='100%' style='width:100%; border-collapse: collapse; margin: 0; padding: 0;'>");
                html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");
                html.append("<td colspan='3' style='white-space:nowrap; -webkit-text-size-adjust:none !important; min-width:200px; max-width:560px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
                html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                html.append("<p style='font-size:14px; line-height:14px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
                html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "POZNÁMKA PRO SKLAD");
                html.append("</p>");
                html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                html.append("</td></tr>");

                html.append("<tr style='background-color: #FFFFFF;'>");
                html.append("<td width='10' style='width:10px; border-left:1px solid #e5ecf4; border-right:0; border-bottom:1px solid #e5ecf4; border-top:0; margin: 0; padding: 0;'></td>");
                html.append("<td style='min-width:200px; max-width:560px; border-left:0; border-right:0; border-bottom:1px solid #e5ecf4; border-top:0; color: #1e1d1d; margin: 0; padding: 0;'>");
                html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                html.append("<p style='font-size:12px; line-height:12px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
                html.append(mojeAplikacniData.getPoznamkaSkladu());
                html.append("</p>");
                html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                html.append("</td>");
                html.append("<td width='10' style='width:10px; border-right:1px solid #e5ecf4; border-left:0; border-bottom:1px solid #e5ecf4; border-top:0; margin: 0; padding: 0;'></td>");
                html.append("</tr>");

                html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");
                html.append("<td colspan='3' style='min-width:200px; max-width:560px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
                html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                html.append("</td></tr></table>");
                html.append("</div>");

                html.append("<p style='font-size:14px; line-height:14px; mso-line-height-rule:exactly; margin:0; padding: 0;'>&nbsp;</p>");
            }
        }

        return html;
    }

    private StringBuilder htmlTrasy(StringBuilder html, List<MyData.OsobaDochazka> osobySkladDistribuce){

        if (nacteneTrasy.size() == 0) return html;

        String oddeleni = osobySkladDistribuce == mojeAplikacniData.getOsobySklad() ? "SKLAD" : "DISTRIBUCE";

        boolean prvniTrasa = true;
        for (int t = 0; t < nacteneTrasy.size(); t++) {
            MyRoute trasa = nacteneTrasy.get(t);

            if (trasa.getDump().isEmpty()) continue;
           // if (!oddeleni.equals("SKLAD") && trasa.factory.equals("1")) { minusVl++; continue; }

            // Mezera mezi tabulkami pro Outlook i Gmail
            if (!prvniTrasa && t > 0) html.append("<p style='font-size:14px; line-height:14px; mso-line-height-rule:exactly; margin:0; padding: 0;'>&nbsp;</p>");
            prvniTrasa = false;

            html.append("<div style='border:1px solid #ffffff; border-radius:10px; overflow:hidden;'>");
            // 0. TABULKA
            html.append("<table cellpadding='0' cellspacing='0' width='100%' style='width:100%; border-collapse: collapse; margin: 0; padding: 0;'>");
            // 1. ŘÁDEK - MODRÁ HLAVIČKA
            html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");
            html.append("<td colspan='3' style='white-space:nowrap; -webkit-text-size-adjust:none !important; min-width:200px; max-width:560px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:14px; line-height:14px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + trasa.posadka);
            if (oddeleni.equals("SKLAD")) html.append(" &nbsp; | &nbsp; NAKLÁDKA ").append(trasa.getStartTime());
            html.append("</p>");
            html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td></tr>");
            // 2. ŘÁDKY - ZASTÁVKY
            ArrayList<MyRouteDump> zastavky = trasa.getDump();
            String trasaGoogleMaps = "https://maps.google.com/maps/dir/My+Location";
            if (zastavky != null) {
                for (int i = 0; i < zastavky.size(); i++) {
                    MyRouteDump zastavka = zastavky.get(i);

                 //   String borderStyle = (i == zastavky.size() - 1) ? "" : "border-bottom:1px solid #e5ecf4;";

                    if (i % 2 == 0) html.append("<tr style='background-color: #FFFFFF;'>");
                    else html.append("<tr style='background-color: #F8FAFC;'>");

                    html.append("<td width='25' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:25px; text-align:center; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
                    html.append("<p style='font-size:12px; line-height:12px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
                    html.append(zastavka.getOrder());
                    html.append("</p></td>");
                    html.append("<td width='475' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:475px; min-width:225px; max-width:485px; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0px; border-left:0px; margin: 0; padding: 0;'>");
                    html.append("<table cellpadding='0' cellspacing='0' width='100%' style='width:100%; border-collapse: collapse; margin: 0; padding: 0;'>");
                    html.append("<tr>");
                    html.append("<td width='5' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:5px; border-left:0; border-right:0; border-top:0; border-bottom:0; margin: 0; padding: 0;'></td>");
                    html.append("<td colspan='2' style='white-space:nowrap; -webkit-text-size-adjust:none !important; min-width:220px; max-width:480px; color: #1e1d1d; border-left:0; border-right:0; border-top:0; border-bottom:0; margin: 0; padding: 0;'>");
                    html.append("<p style='width:100%; line-height:5px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                    html.append("<p style='font-size:12px; line-height:12px; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
                    html.append(zastavka.getName());
                    html.append("</p>");
                    html.append("<p style='width:100%; line-height:2px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                    html.append("</td></tr>");
                    html.append("<tr>");
                    html.append("<td width='5' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:5px; border-left:0; border-right:0; border-top:0; border-bottom:0; margin: 0; padding: 0;'></td>");
                    html.append("<td width='5' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:5px; border-left:0; border-right:0; border-top:0; border-bottom:0; margin: 0; padding: 0;'></td>");
                    html.append("<td width='465' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:465px; min-width:215px; max-width:475px; color: #718096; margin: 0; padding: 0;'>");
                    html.append("<p style='font-size:11px; line-height:11px; mso-line-height-rule:exactly; text-decoration: none !important; color: #718096 !important; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
                    if (oddeleni.equals("SKLAD")) html.append("<a href='https://maps.google.com/?q=" + zastavka.gpsLat + "," + zastavka.gpsLng + "' style='text-decoration: none !important; color: #718096 !important; mso-effects-shadow: none; mso-text-shadow: none;'>");
                    html.append(omezString(zastavka.getAddress() + " " + zastavka.getCity(), 35));
                    if (oddeleni.equals("SKLAD")) html.append("</a>");
                    html.append("</p>");
                    html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
                    html.append("</td></tr></table></td>");
                    html.append("<td width='60' style='white-space:nowrap; -webkit-text-size-adjust:none !important; width:60px; text-align:center; border-left:1px solid #e5ecf4; border-right:1px solid #e5ecf4; border-bottom:1px solid #e5ecf4; border-top:0; color: #718096; margin: 0; padding: 0;'>");
                    html.append("<p style='font-size:12px; line-height:12px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
                    html.append(zastavka.getTime());
                    html.append("</p></td></tr>");

                    String pridatDoTrasy = "/" + zastavka.gpsLat.replace(",", ".") + "+" + zastavka.gpsLng.replace(",", ".");
                    if (!trasaGoogleMaps.contains(pridatDoTrasy)) trasaGoogleMaps += pridatDoTrasy;
                }
            }

            String gpsLat = "", gpsLng = "";
            for (MyData.Lokace l: mojeAplikacniData.getLokace()) if (l.isVychoziDc()) { gpsLat = l.getGpsLat(); gpsLng = l.getGpsLng(); break; }

            trasaGoogleMaps = trasaGoogleMaps + "/" + gpsLat + "+" + gpsLng;

            // 3. ŘÁDEK - SUMÁŘ
            html.append("<tr style='color:#FFFFFF; background-color: #3f7ec1;'>");
            html.append("<td colspan='3' style='white-space:nowrap; -webkit-text-size-adjust:none !important; min-width:200px; max-width:560px; border-left: 1px solid #3f7ec1; border-right: 1px solid #3f7ec1; border-top: 1px solid #3f7ec1; border-bottom: 0px; margin: 0; padding: 0;'>");
            html.append("<p style='width:100%; line-height:4px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("<p style='font-size:12px; line-height:12px; text-decoration: none !important; color: #FFFFFF !important; mso-line-height-rule:exactly; margin: 0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>");
            if (oddeleni.equals("SKLAD")) {
                if (trasa.factory.equals("13")) {
                    html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "KONEC ").append(trasa.getEndTime())
                            .append(" &nbsp; | &nbsp; HM. ").append(trasa.getTonne())
                            .append("<span style='font-size: 7px;'> t</span>")
                            .append(" &nbsp; | &nbsp; ")
                            .append("<a href='" + trasaGoogleMaps + "' style='text-decoration: none !important; color: #718096 !important; mso-effects-shadow: none; mso-text-shadow: none;'>")
                            .append("<font color='#fffffe' style='color: #fffffe !important;'>")
                            .append("TRASA ")
                            .append(trasa.getKm())
                            .append("<span style='font-size: 7px;'> km</span>")
                            .append("</font></a>");
                }
                else html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "POZNÁMKA: ").append(trasa.getEndTime())
                        .append(" &nbsp; | &nbsp; ")
                        .append("<a href='" + trasaGoogleMaps + "' style='text-decoration: none !important; color: #718096 !important; mso-effects-shadow: none; mso-text-shadow: none;'>")
                        .append("<font color='#fffffe' style='color: #fffffe !important;'>")
                        .append("TRASA ")
                        .append(trasa.getKm())
                        .append("<span style='font-size: 7px;'> km</span>")
                        .append("</font></a>");;
            }
            else {
                html.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "TEL: ");
                html.append("<a href='' style='text-decoration: none !important; color: #718096 !important; mso-effects-shadow: none; mso-text-shadow: none;'>");
                html.append("<font color='#fffffe' style='color: #fffffe !important;'>");
                html.append(trasa.telefon);
                html.append("</font></a>");
            }
            html.append("</p>");
            html.append("<p style='width:100%; line-height:3px; mso-line-height-rule:exactly; margin:0; padding: 0; mso-margin-top-alt:0px; mso-margin-bottom-alt:0px;'>&nbsp;</p>");
            html.append("</td></tr></table>");
            html.append("</div>");
        }

        return html;
    }
    private String testCas(String text) {
        if (text == null || text.isEmpty()) {
            return Integer.toString(12);
        }
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (!Character.isDigit(c) && c != ':' && c != '.') {
                return Integer.toString(8);
            }
        }
        return Integer.toString(12);
    }
    private String omezString(String text, int max) {
        if (text == null) {
            return null;
        }
        // 1. Zkrácení textu
        text = text.length() > max ? text.substring(0, max) : text;

        // 2. Nahrazení mezery mezerou a "Word Joinerem" (pro oko mezera, pro Gmail stopka)
        // Použijeme entitu &#8288; (U+2060), kterou Gmail nemaže a neslepuje
        text = text.replace(" ", " &#8288;");

        return text;
    }

    private String importujDataObchodXml(String vstupniDatum) {
        String cestaKXml = "S:\\Prodeje KBT\\Objednavky\\Zdroj dat\\Objednavky.xml";
        String hledaneDatum;
        LocalDate datum;

        try {
            DateTimeFormatter vstupniFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            datum = LocalDate.parse(vstupniDatum, vstupniFormatter);
            DateTimeFormatter vystupniFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            hledaneDatum = datum.format(vystupniFormatter);
        } catch (Exception e) {
            datum = LocalDate.now().plusDays(1);
            hledaneDatum = datum.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        DateTimeFormatter infoDate = DateTimeFormatter.ofPattern("eeee dd.MM.yyyy", new Locale("cs", "CZ"));

        XmlImportDataObchod data = XmlDataImporter.importujDataZXml(cestaKXml, hledaneDatum);

        casPosledniAktualizaceObchod = data.casPosledniAktualizace;

        dataOz.clear();
        dataOblast.clear();
        if (data.dataOz.isEmpty() || data.dataOblasti.isEmpty()) {
            dataOz.add(new ImportDataObchod("Chyba načtení aktuálních dat na den " + datum.format(infoDate), 0.0));
            dataOblast.add(new ImportDataObchod("Chyba načtení aktuálních dat na den " + datum.format(infoDate), 0.0));
            return datum.format(infoDate);
        }

        if (mojeAplikacniData.getObchodnici().isEmpty()) dataOz.add(new ImportDataObchod("Přidejte v číselníkách hledené obchodníky", 0.0));

        for (Map.Entry<String, Double> entry : data.dataOz.entrySet()) {
            for (MyData.OsobaObchod e: mojeAplikacniData.getObchodnici()){
                if (e.getJmeno().equals(entry.getKey())){
                    dataOz.add(new ImportDataObchod(entry.getKey(), entry.getValue()));
                    break;
                }
            }
        }
        for (Map.Entry<String, Double> entry : data.dataOblasti.entrySet()) {
            if (entry.getKey().trim().equals("V") || entry.getKey().trim().equals("S") || entry.getKey().trim().equals("Z") ||
                    entry.getKey().trim().equals("90") || entry.getKey().trim().equals("91"))
                        dataOblast.add(new ImportDataObchod(entry.getKey(), entry.getValue()));
        }
        return datum.format(infoDate);
    }

    public void ulozDoHtml(String obsahCsv, String nazevSouboruNew) {
        Path slozka = Paths.get("");

        try {
            if (Files.notExists(slozka)) {
                Files.createDirectories(slozka);
            }

            Path cestaKSouboruNew = slozka.resolve(nazevSouboruNew);


            try (BufferedWriter writer = Files.newBufferedWriter(cestaKSouboruNew,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {

                writer.write('\ufeff');
                writer.write(obsahCsv);
            }

        } catch (IOException e) {
            System.err.println("Chyba při práci se souborem: " + e.getMessage());
        }
    }
}