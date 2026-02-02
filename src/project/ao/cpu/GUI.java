package project.ao.cpu;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
public class GUI {

    private static JTextArea codeArea;
    private static JFrame programDisplayFrame;
    private static JTextArea programDisplayArea;
    private static JFrame ramWindow;
    private static JFrame romWindow;
    private static Memoire memoire;
    private static RAM ram;
    private static ROM rom;
    private static Cpu cpu;
    private static Register reg;
    private static JTextField pcField, aField, bField, xField, yField, sField, uField, dpField1;
    private static JTextField flagsField;
    private static volatile boolean executionRunning = false;
    private static Thread executionThread = null;
    private static JButton runBtn;
    private static JButton stopBtn;
    private static JTextArea ramTextArea;
    private static JTextArea romTextArea;

    public static void main(String[] args) {
        byte[] programmeVide = new byte[1024];
        ram = new RAM();
        rom = new ROM(programmeVide);
        memoire = new Memoire(programmeVide);
        ram = memoire.getRam();
        rom = memoire.getRom();

        reg = new Register();
        cpu = new Cpu(memoire, reg);
        cpu.reset();

        // Fenêtre principale : MENU + BARRE D'OUTILS ===
        JFrame menuFrame = new JFrame("MOTO6809 - Menu");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(2000, 120);
        menuFrame.setLocation(0, 0);
        menuFrame.getContentPane().setBackground(Color.PINK);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Fichier");
        fileMenu.add(new JMenuItem("Nouveau"));

        // Menu Ouvrir
        JMenuItem ouvrirMenuItem = new JMenuItem("Ouvrir");
        ouvrirMenuItem.addActionListener(e -> {
            System.out.println("🔵 Menu Ouvrir cliqué !");
            ouvrirFichier();
        });
        fileMenu.add(ouvrirMenuItem);

        //  Menu Enregistrer
        JMenuItem enregistrerMenuItem = new JMenuItem("Enregistrer");
        enregistrerMenuItem.addActionListener(e -> {
            System.out.println("🔵 Menu Enregistrer cliqué !");
            enregistrerFichier();
        });
        fileMenu.add(enregistrerMenuItem);

        fileMenu.add(new JMenuItem("Enregistrer sous..."));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Imprimer"));
        fileMenu.addSeparator();
        JMenuItem assemblerItem = new JMenuItem("Assembler");
        assemblerItem.addActionListener(e -> assemblerCode());
        fileMenu.add(assemblerItem);
        fileMenu.add(new JMenuItem("Quitter"));

        JMenu simMenu = new JMenu("Simulation");
        simMenu.add(new JMenuItem("Exécuter"));
        simMenu.add(new JMenuItem("Pas à Pas"));
        simMenu.add(new JMenuItem("Défaire"));
        simMenu.addSeparator();
        simMenu.add(new JMenuItem("Reset"));

        JMenu toolsMenu = new JMenu("Outils");
        toolsMenu.add(new JMenuItem("Editeur"));
        toolsMenu.add(new JMenuItem("Calculatrice"));
        toolsMenu.addSeparator();
        toolsMenu.add(new JMenuItem("Information"));

        JMenu windowsMenu = new JMenu("Fenêtres");
        JMenuItem programItem = new JMenuItem("Programme");
        programItem.addActionListener(e -> showProgramWindow());
        windowsMenu.add(programItem);

        JMenuItem ramItem = new JMenuItem("RAM");
        ramItem.addActionListener(e -> showRamWindow());
        windowsMenu.add(ramItem);

        JMenuItem romItem = new JMenuItem("ROM");
        romItem.addActionListener(e -> showRomWindow());
        windowsMenu.add(romItem);

        windowsMenu.add(new JMenuItem("PIA"));
        windowsMenu.addSeparator();
        windowsMenu.add(new JMenuItem("Arranger"));

        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.add(new JMenuItem("Police"));
        optionsMenu.add(new JMenuItem("Configuration"));
        optionsMenu.addSeparator();
        optionsMenu.add(new JMenuItem("Sauver"));

        JMenu helpMenu = new JMenu("Aide");
        helpMenu.add(new JMenuItem("À propos..."));
        helpMenu.addSeparator();
        helpMenu.add(new JMenuItem("Index"));
        helpMenu.add(new JMenuItem("jeu d'instruction"));

        menuBar.add(fileMenu);
        menuBar.add(simMenu);
        menuBar.add(toolsMenu);
        menuBar.add(windowsMenu);
        menuBar.add(optionsMenu);
        menuBar.add(helpMenu);
        menuFrame.setJMenuBar(menuBar);

        JToolBar toolBar = new JToolBar();

        //  Bouton Ouvrir
        JButton openBtn = new JButton("📂");
        openBtn.setToolTipText("Ouvrir un programme assembleur");
        openBtn.addActionListener(e -> {
            System.out.println("🔵 Bouton Ouvrir cliqué !");
            ouvrirFichier();
        });
        toolBar.add(openBtn);

        //  Bouton Enregistrer
        JButton saveBtn = new JButton("💾");
        saveBtn.setToolTipText("Enregistrer le programme actuel");
        saveBtn.addActionListener(e -> {
            System.out.println("🔵 Bouton Enregistrer cliqué !");
            enregistrerFichier();
        });
        toolBar.add(saveBtn);

        JButton printBtn = new JButton("🖨");
        printBtn.setToolTipText("Imprimer les résultats ou le code");
        toolBar.add(printBtn);

        toolBar.addSeparator();

        JButton resetBtn = new JButton("RESET");
        resetBtn.setToolTipText("Réinitialiser le microprocesseur");
        resetBtn.addActionListener(e -> {
            cpu.reset();
            refreshRegisters();
            refreshRamDisplay();
            refreshRomDisplay();
        });
        toolBar.add(resetBtn);

        JButton irqBtn = new JButton("IRQ");
        irqBtn.setToolTipText("Simuler une interruption standard (IRQ)");
        toolBar.add(irqBtn);

        JButton firqBtn = new JButton("FIRQ");
        firqBtn.setToolTipText("Simuler une interruption rapide (FIRQ)");
        toolBar.add(firqBtn);

        JButton nmiBtn = new JButton("NMI");
        nmiBtn.setToolTipText("Simuler une interruption non masquable (NMI)");
        toolBar.add(nmiBtn);

        toolBar.addSeparator();

        runBtn = new JButton("▶ Exécuter");
        runBtn.setToolTipText("Exécuter en continu jusqu'à SWI/END");
        runBtn.addActionListener(e -> {
            if (!executionRunning) {
                executionRunning = true;
                executionThread = new Thread(() -> {
                    try {
                        while (executionRunning && !cpu.isHalted()) {
                            cpu.step();
                            SwingUtilities.invokeLater(() -> {
                                refreshRegisters();
                                refreshRamDisplay();
                                refreshRomDisplay();
                            });
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    } finally {
                        executionRunning = false;
                        SwingUtilities.invokeLater(() -> {
                            runBtn.setEnabled(true);
                            stopBtn.setEnabled(false);
                        });
                    }
                });
                executionThread.start();
                runBtn.setEnabled(false);
                stopBtn.setEnabled(true);
            }
        });
        toolBar.add(runBtn);

        stopBtn = new JButton("⏹ STOP");
        stopBtn.setToolTipText("Arrêter l'exécution continue");
        stopBtn.setEnabled(false);
        stopBtn.addActionListener(e -> {
            executionRunning = false;
            if (executionThread != null) {
                executionThread.interrupt();
            }
        });
        toolBar.add(stopBtn);

        JButton stepBtn = new JButton("👣");
        stepBtn.setToolTipText("Exécuter une instruction (mode pas à pas)");
        stepBtn.addActionListener(e -> {
            cpu.step();
            refreshRegisters();
            refreshRamDisplay();
            refreshRomDisplay();
        });
        toolBar.add(stepBtn);

        JButton editBtn = new JButton("📝");
        editBtn.setToolTipText("Passer en mode édition du programme");
        toolBar.add(editBtn);

        menuFrame.add(toolBar, BorderLayout.NORTH);
        menuFrame.setVisible(true);

        // === 2. Fenêtre : REGISTRES ===
        JFrame registersFrame = new JFrame("Registres");
        registersFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        registersFrame.setSize(350, 500);
        registersFrame.setLocation(0, 130);
        registersFrame.getContentPane().setBackground(Color.PINK);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.PINK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel pcPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pcPanel.setBackground(Color.PINK);
        JLabel pcLabel = new JLabel("PC");
        pcLabel.setFont(new Font("Arial", Font.BOLD, 20));
        pcPanel.add(pcLabel);
        pcField = new JTextField("FC00", 4);
        pcField.setEditable(false);
        pcField.setFont(new Font("Arial", Font.BOLD, 20));
        pcField.setForeground(Color.BLUE);
        pcField.setBackground(Color.WHITE);
        pcField.setHorizontalAlignment(JTextField.CENTER);
        pcField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        pcPanel.add(pcField);
        mainPanel.add(pcPanel);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setBackground(Color.PINK);
        JTextField emptyField = new JTextField("         ", 30);
        emptyField.setEditable(false);
        emptyField.setPreferredSize(new Dimension(200, 30));
        emptyField.setBackground(Color.WHITE);
        emptyField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        emptyPanel.add(emptyField);
        mainPanel.add(emptyPanel);

        JPanel suPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        suPanel.setBackground(Color.PINK);
        JLabel sLabel = new JLabel("S");
        sLabel.setFont(new Font("Arial", Font.BOLD, 20));
        suPanel.add(sLabel);
        sField = new JTextField("0000", 4);
        sField.setEditable(false);
        sField.setFont(new Font("Arial", Font.BOLD, 20));
        sField.setForeground(Color.BLUE);
        sField.setBackground(Color.WHITE);
        sField.setHorizontalAlignment(JTextField.CENTER);
        sField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        suPanel.add(sField);
        JLabel uLabel = new JLabel("U");
        uLabel.setFont(new Font("Arial", Font.BOLD, 20));
        suPanel.add(uLabel);
        uField = new JTextField("0000", 4);
        uField.setEditable(false);
        uField.setFont(new Font("Arial", Font.BOLD, 20));
        uField.setForeground(Color.BLUE);
        uField.setBackground(Color.WHITE);
        uField.setHorizontalAlignment(JTextField.CENTER);
        uField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        suPanel.add(uField);
        mainPanel.add(suPanel);

        JPanel panel = new JPanel(null);
        panel.setBackground(Color.PINK);
        panel.setPreferredSize(new Dimension(350, 350));

        JLabel aLabel = new JLabel("A");
        aLabel.setFont(new Font("Arial", Font.BOLD, 20));
        aLabel.setBounds(20, 70, 25, 30);
        panel.add(aLabel);
        aField = new JTextField("00");
        aField.setEditable(false);
        aField.setFont(new Font("Arial", Font.BOLD, 20));
        aField.setForeground(Color.BLUE);
        aField.setHorizontalAlignment(JTextField.CENTER);
        aField.setBounds(45, 70, 50, 30);
        panel.add(aField);

        JLabel bLabel = new JLabel("B");
        bLabel.setFont(new Font("Arial", Font.BOLD, 20));
        bLabel.setBounds(20, 130, 25, 30);
        panel.add(bLabel);
        bField = new JTextField("00");
        bField.setEditable(false);
        bField.setFont(new Font("Arial", Font.BOLD, 20));
        bField.setForeground(Color.BLUE);
        bField.setHorizontalAlignment(JTextField.CENTER);
        bField.setBounds(45, 130, 50, 30);
        panel.add(bField);

        JLabel dpLabel = new JLabel("DP");
        dpLabel.setFont(new Font("Arial", Font.BOLD, 20));
        dpLabel.setBounds(20, 180, 40, 30);
        panel.add(dpLabel);
        dpField1 = new JTextField("00");
        dpField1.setEditable(false);
        dpField1.setFont(new Font("Arial", Font.BOLD, 20));
        dpField1.setForeground(Color.BLUE);
        dpField1.setHorizontalAlignment(JTextField.CENTER);
        dpField1.setBounds(60, 180, 50, 30);
        panel.add(dpField1);
        JTextField dpField2 = new JTextField("00000100");
        dpField2.setEditable(false);
        dpField2.setFont(new Font("Arial", Font.BOLD, 16));
        dpField2.setForeground(Color.BLUE);
        dpField2.setHorizontalAlignment(JTextField.CENTER);
        dpField2.setBounds(120, 180, 140, 30);
        panel.add(dpField2);

        int ualWidth = 130;
        int ualHeight = 80;
        int ualX = 110;
        int ualY = 70;
        JPanel ualPanel = new JPanel();
        ualPanel.setBackground(Color.LIGHT_GRAY);
        ualPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        ualPanel.setBounds(ualX, ualY, ualWidth, ualHeight);
        JLabel ualLabel = new JLabel("UAL", SwingConstants.CENTER);
        ualLabel.setFont(new Font("Arial", Font.BOLD, 20));
        ualPanel.add(ualLabel);
        panel.add(ualPanel);

        flagsField = new JTextField("0 0 0 0 0 0 0 0", 16);
        flagsField.setEditable(false);
        flagsField.setFont(new Font("Arial", Font.BOLD, 14));
        flagsField.setHorizontalAlignment(JTextField.CENTER);
        flagsField.setBounds(120, 215, 150, 20);
        panel.add(flagsField);

        JPanel lignes = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.BLACK);
                int aX = aField.getX() + aField.getWidth();
                int aY = aField.getY() + aField.getHeight() / 2;
                int bX = bField.getX() + bField.getWidth();
                int bY = bField.getY() + bField.getHeight() / 2;
                int dp2Y = dpField2.getY() + dpField2.getHeight() / 2;
                int ualLeft = ualPanel.getX();
                int ualCentreX = ualLeft + ualWidth / 2;
                int ualBottom = ualPanel.getY() + ualPanel.getHeight();
                g.drawLine(aX, aY, ualLeft, aY);
                g.drawLine(bX, bY, ualLeft, bY);
                g.drawLine(ualCentreX, ualBottom, ualCentreX, dp2Y);
            }
        };
        lignes.setBounds(0, 0, 350, 350);
        lignes.setOpaque(false);
        panel.add(lignes);

        mainPanel.add(panel);

        JPanel xyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        xyPanel.setBackground(Color.PINK);
        JLabel xLabel = new JLabel("X");
        xLabel.setFont(new Font("Arial", Font.BOLD, 20));
        xyPanel.add(xLabel);
        xField = new JTextField("0000", 4);
        xField.setEditable(false);
        xField.setFont(new Font("Arial", Font.BOLD, 20));
        xField.setForeground(Color.BLUE);
        xField.setBackground(Color.WHITE);
        xField.setHorizontalAlignment(JTextField.CENTER);
        xField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        xyPanel.add(xField);
        JLabel yLabel = new JLabel("Y");
        yLabel.setFont(new Font("Arial", Font.BOLD, 20));
        xyPanel.add(yLabel);
        yField = new JTextField("0000", 4);
        yField.setEditable(false);
        yField.setFont(new Font("Arial", Font.BOLD, 20));
        yField.setForeground(Color.BLUE);
        yField.setBackground(Color.WHITE);
        yField.setHorizontalAlignment(JTextField.CENTER);
        yField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        xyPanel.add(yField);
        mainPanel.add(xyPanel);

        registersFrame.add(mainPanel);
        registersFrame.setVisible(true);

        //  PROGRAMME (Éditeur) ===
        JFrame programFrame = new JFrame("Edit...");
        programFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        programFrame.setSize(250, 300);
        programFrame.setLocation(360, 130);
        programFrame.getContentPane().setBackground(Color.PINK);

        JToolBar editorToolBar = new JToolBar();
        editorToolBar.setFloatable(false);

        JButton applyBtn = new JButton("🔄");
        applyBtn.setToolTipText("Prendre en compte les modifications de l'assembleur");
        editorToolBar.add(applyBtn);
        applyBtn.addActionListener(e -> assemblerCode());

        JButton findBtn = new JButton("🔍");
        findBtn.setToolTipText("Chercher un mot dans le texte");
        editorToolBar.add(findBtn);

        JButton helpBtn = new JButton("📖");
        helpBtn.setToolTipText("Ouvrir le fichier d'aide sur l'instruction sélectionnée");
        editorToolBar.add(helpBtn);

        JButton printCodeBtn = new JButton("📄🖨");
        printCodeBtn.setToolTipText("Imprimer le contenu du programme");
        editorToolBar.add(printCodeBtn);

        JButton closeBtn = new JButton("❌");
        closeBtn.setToolTipText("Quitter l'éditeur");
        closeBtn.addActionListener(e -> programFrame.dispose());
        editorToolBar.add(closeBtn);

        editorToolBar.addSeparator();
        editorToolBar.add(new JLabel("Mise à jour"));
        editorToolBar.addSeparator();
        editorToolBar.add(new JLabel("Edition"));

        programFrame.add(editorToolBar, BorderLayout.NORTH);

        codeArea = new JTextArea();
        codeArea.setFont(new Font("Courier New", Font.PLAIN, 14));
        codeArea.setLineWrap(false);
        codeArea.setBackground(Color.PINK);
        codeArea.setForeground(Color.BLACK);
        codeArea.setOpaque(true);

        programFrame.add(new JScrollPane(codeArea), BorderLayout.CENTER);
        programFrame.setVisible(true);
    }


    //  MÉTHODE OUVRIR FICHIER

    private static void ouvrirFichier() {
        System.out.println("✅ ouvrirFichier() appelée");

        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Fichiers Assembleur (*.asm, *.s, *.txt)", "asm", "s", "txt");
            fileChooser.setFileFilter(filter);
            fileChooser.setAcceptAllFileFilterUsed(true);

            fileChooser.setDialogTitle("Ouvrir un programme assembleur");

            int resultat = fileChooser.showOpenDialog(null);

            if (resultat == JFileChooser.APPROVE_OPTION) {
                File fichier = fileChooser.getSelectedFile();
                System.out.println("📁 Fichier sélectionné : " + fichier.getAbsolutePath());

                if (!fichier.exists()) {
                    JOptionPane.showMessageDialog(null,
                            "Le fichier n'existe pas !",
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String contenu = lireFichier(fichier);

                if (codeArea != null) {
                    codeArea.setText(contenu);
                    System.out.println("✅ Contenu chargé dans l'éditeur");
                }

                JOptionPane.showMessageDialog(null,
                        "Fichier chargé avec succès !\n" +
                                "Fichier : " + fichier.getName() + "\n" +
                                "Lignes : " + contenu.split("\n").length,
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Erreur lors de l'ouverture du fichier :\n" + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    // MÉTHODE LIRE FICHIER

    private static String lireFichier(File fichier) throws IOException {
        StringBuilder contenu = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(fichier))) {
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                contenu.append(ligne).append("\n");
            }
        }

        return contenu.toString();
    }


    //MÉTHODE ENREGISTRER FICHIER

    private static void enregistrerFichier() {
        System.out.println("✅ enregistrerFichier() appelée");

        if (codeArea == null || codeArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Rien à enregistrer !",
                    "Attention",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "Fichiers Assembleur (*.asm)", "asm");
            fileChooser.setFileFilter(filter);
            fileChooser.setDialogTitle("Enregistrer le programme");

            int resultat = fileChooser.showSaveDialog(null);

            if (resultat == JFileChooser.APPROVE_OPTION) {
                File fichier = fileChooser.getSelectedFile();

                if (!fichier.getName().toLowerCase().endsWith(".asm")) {
                    fichier = new File(fichier.getAbsolutePath() + ".asm");
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fichier))) {
                    writer.write(codeArea.getText());
                }

                JOptionPane.showMessageDialog(null,
                        "Fichier enregistré avec succès !\n" + fichier.getName(),
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Erreur lors de l'enregistrement :\n" + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showProgramWindow() {
        if (programDisplayFrame == null) {
            programDisplayFrame = new JFrame("Programme");
            programDisplayFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            programDisplayFrame.setSize(300, 250);
            programDisplayFrame.setLocation(400, 200);

            programDisplayArea = new JTextArea();
            programDisplayArea.setEditable(false);
            programDisplayArea.setFont(new Font("ARIAL", Font.PLAIN, 14));
            programDisplayArea.setBackground(Color.WHITE);
            programDisplayArea.setLineWrap(true);
            programDisplayArea.setWrapStyleWord(true);

            programDisplayFrame.add(new JScrollPane(programDisplayArea));
        }

        // MISE À JOUR DU CONTENU + AFFICHAGE
        if (programDisplayArea != null && codeArea != null) {
            programDisplayArea.setText(codeArea.getText());
        }

        programDisplayFrame.setVisible(true);
        programDisplayFrame.toFront();
    }

    private static void showRamWindow() {
        if (ramWindow == null) {
            ramWindow = new JFrame("RAM");
            ramWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            ramWindow.setSize(220, 300);
            ramWindow.setLocation(720, 130);

            ramTextArea = new JTextArea();
            ramTextArea.setEditable(false);
            ramTextArea.setFont(new Font("ARIAL", Font.PLAIN, 14));
            ramTextArea.setBackground(Color.WHITE);

            ramWindow.add(new JScrollPane(ramTextArea));
        }

        ramWindow.setVisible(true);
        ramWindow.toFront();
        refreshRamDisplay();
    }

    private static void showRomWindow() {
        if (romWindow == null) {
            romWindow = new JFrame("ROM");
            romWindow.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            romWindow.setSize(220, 300);
            romWindow.setLocation(950, 130);

            romTextArea = new JTextArea();
            romTextArea.setEditable(false);
            romTextArea.setFont(new Font("ARIAL", Font.PLAIN, 14));
            romTextArea.setBackground(Color.WHITE);

            romWindow.add(new JScrollPane(romTextArea));
        }

        romWindow.setVisible(true);
        romWindow.toFront();
        refreshRomDisplay();
    }

    private static void refreshRegisters() {
        pcField.setText(String.format("%04X", reg.getPC() & 0xFFFF));
        aField.setText(String.format("%02X", reg.getA() & 0xFF));
        bField.setText(String.format("%02X", reg.getB() & 0xFF));
        xField.setText(String.format("%04X", reg.getX() & 0xFFFF));
        yField.setText(String.format("%04X", reg.getY() & 0xFFFF));
        sField.setText(String.format("%04X", reg.getSP() & 0xFFFF));
        uField.setText(String.format("%04X", reg.getU() & 0xFFFF));
        dpField1.setText(String.format("%02X", reg.getDP() & 0xFF));

        byte f = reg.getFlags();
        String flags = String.format("%d %d %d %d %d %d %d %d",
                (f >> 7) & 1, (f >> 6) & 1, (f >> 5) & 1, (f >> 4) & 1,
                (f >> 3) & 1, (f >> 2) & 1, (f >> 1) & 1, f & 1);
        flagsField.setText(flags);
    }

    private static void refreshProgramDisplay() {
        if (programDisplayArea != null && programDisplayFrame != null && programDisplayFrame.isVisible()) {
            programDisplayArea.setText(codeArea.getText());
        }
    }

    private static void assemblerCode() {
        System.out.println(" assemblerCode() appelé !");
        try {
            String code = codeArea.getText();
            if (code == null || code.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Le code est vide !", "Erreur", JOptionPane.WARNING_MESSAGE);
                return;
            }
            System.out.println("Code à assembler :\n" + code);

            String[] lignes = code.split("\n");
            byte[] romContent = new byte[1024];

            int addr = 0;
            for (String ligne : lignes) {
                ligne = ligne.trim();

                //  Ignorer lignes vides et commentaires
                if (ligne.isEmpty() || ligne.startsWith(";")) continue;

                // Si END est trouvé, arrêter l'assemblage
                if (ligne.toUpperCase().equals("END")) {
                    System.out.println("✅ Directive END trouvée - Fin de l'assemblage");
                    break;
                }

                String[] parties = ligne.split("\\s+", 2);
                String opcode = parties[0];
                String operand = parties.length > 1 ? parties[1] : "";

                Instruction instr = new Instruction(opcode, operand);

                if (!instr.estSyntaxeValide()) {
                    JOptionPane.showMessageDialog(null,
                            "Erreur à la ligne : " + ligne + "\n" + instr.getMessageErreur(),
                            "Erreur d'assemblage", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                byte[] octets = instr.assemble();

                if (addr + octets.length > 1024) {
                    JOptionPane.showMessageDialog(null,
                            "ROM débordée à la ligne : " + ligne,
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for (byte b : octets) {
                    romContent[addr++] = b;
                }
            }

            // Affichage debug
            System.out.println("ROM assemblée (" + addr + " octets) :");
            for (int i = 0; i < addr; i++) {
                System.out.printf("%02X ", romContent[i] & 0xFF);
                if ((i + 1) % 16 == 0) System.out.println();
            }
            System.out.println();

            memoire = new Memoire(romContent);
            ram = memoire.getRam();
            rom = memoire.getRom();

            cpu = new Cpu(memoire, reg);
            cpu.reset();
            refreshRegisters();
            refreshRamDisplay();
            refreshRomDisplay();

            JOptionPane.showMessageDialog(null, "Assemblage réussi ! (" + addr + " octets)", "Succès", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Erreur : " + e.getMessage(),
                    "Exception", JOptionPane.ERROR_MESSAGE);
        }
    }

    //  Affichage RAM




    private static void refreshRamDisplay() {
        if (ramTextArea == null) return;
        ramWindow.getContentPane().removeAll();
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 14));
        StringBuilder sb = new StringBuilder();
        Memoire mem = cpu.getMem();
        for (int addr = 0x0000; addr <= 0x03FF; addr++) {
            byte b = mem.read((short) addr);
            if (b != (byte)0xFF) {
                sb.append(String.format("%04X: %02X\n", addr, b & 0xFF));
            }}
        area.setText(sb.toString());
        ramWindow.add(new JScrollPane(area));
        ramWindow.revalidate();
        ramWindow.repaint();
    }

    //  Affichage ROM
    private static void refreshRomDisplay() {
        if (romTextArea != null && romWindow != null && romWindow.isVisible()) {
            StringBuilder sb = new StringBuilder();

            for (int addr = 0xFC00; addr <= 0xFFFF; addr++) {
                byte b = memoire.read((short) addr);
                if (b != (byte)0xFF) { // Afficher seulement les valeurs modifiées
                    sb.append(String.format("%04X  : %02X\n", addr, b & 0xFF));
                }
            }




            romTextArea.setText(sb.toString());
        }
    }

}