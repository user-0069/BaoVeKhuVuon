package com.team.gardendefense.editor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.team.gardendefense.model.level.LevelData;
import com.team.gardendefense.model.level.SpawnerData;
import com.team.gardendefense.model.level.WaveData;
import com.team.gardendefense.model.level.Waypoint;
import com.team.gardendefense.utils.Constants;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class LevelEditor extends JFrame {

    // --- CẤU HÌNH MAP HIỆN TẠI ---
    private int cols = Constants.COLS; // 16
    private int rows = Constants.ROWS; // 12
    private int[][] mapData;

    // --- LEVEL METADATA ---
    private int levelId = 1;
    private String levelName = "Level 1: The Beginning";
    private int initialSun = 100;

    // --- ALLOWED PLANTS ---
    // ID 1 to 10
    private JCheckBox[] plantCheckBoxes;
    private static final String[] PLANT_NAMES = {
        "1. Sunflower (50 Sun)",
        "2. Peashooter R (50 Sun)",
        "3. Sniper R (100 Sun)",
        "4. WallNut (50 Sun)",
        "5. SnowPea R (150 Sun)",
        "6. CherryBomb (150 Sun)",
        "7. Chomper (150 Sun)",
        "8. Peashooter L (50 Sun)",
        "9. Sniper L (100 Sun)",
        "10. SnowPea L (150 Sun)"
    };

    // --- WAVES & SPAWNERS ---
    private List<WaveData> wavesList = new ArrayList<>();
    private int selectedWaveIndex = -1;
    private int selectedSpawnerIndex = -1;

    // --- EDITING MODES & PALETTES ---
    public enum EditMode { PAINT_TILE, PLACE_WAYPOINT }
    private EditMode currentMode = EditMode.PAINT_TILE;
    private int currentBrush = 1; // 0: Cỏ, 1: Đường, 2: Chậu/Bệ

    // --- UI COMPONENTS ---
    private MapCanvas mapCanvas;
    private JTextField txtLevelId;
    private JTextField txtLevelName;
    private JSpinner spinInitialSun;
    private JSpinner spinCols;
    private JSpinner spinRows;

    // Wave UI
    private DefaultListModel<String> waveListModel;
    private JList<String> waveJList;
    private JSpinner spinDelayBefore;

    // Spawner UI
    private DefaultListModel<String> spawnerListModel;
    private JList<String> spawnerJList;
    private JSpinner spinSpawnInterval;
    private JTextField txtZombies;
    private DefaultListModel<String> waypointListModel;
    private JList<String> waypointJList;
    private JToggleButton btnToggleWaypointMode;
    private JLabel lblStatus;

    public LevelEditor() {
        super("Garden Defense - Level Editor (Bộ Thiết Kế Màn Chơi)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1350, 850);
        setLocationRelativeTo(null);

        // Khởi tạo map mặc định
        initDefaultMap();
        initDefaultWaves();

        // Xây dựng giao diện
        buildUI();
    }

    private void initDefaultMap() {
        mapData = new int[rows][cols];
        // Mặc định phủ cỏ (0)
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                mapData[r][c] = 0;
            }
        }
    }

    private void initDefaultWaves() {
        wavesList.clear();
        WaveData w1 = new WaveData();
        w1.delayBefore = 300;
        
        SpawnerData s1 = new SpawnerData();
        s1.spawnInterval = 150;
        s1.zombies = new int[]{1, 1};
        s1.waypoints = new Waypoint[]{
            createWp(-1, 5), createWp(10, 5), createWp(10, 9), createWp(16, 9)
        };
        w1.spawners = new SpawnerData[]{ s1 };

        wavesList.add(w1);
        selectedWaveIndex = 0;
        selectedSpawnerIndex = 0;
    }

    private Waypoint createWp(int c, int r) {
        Waypoint wp = new Waypoint();
        wp.col = c; wp.row = r;
        return wp;
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // 1. TOP TOOLBAR: Thông tin cơ bản & File IO
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topPanel.setBackground(new Color(240, 240, 245));
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        topPanel.add(new JLabel("Level ID:"));
        txtLevelId = new JTextField(String.valueOf(levelId), 3);
        topPanel.add(txtLevelId);

        topPanel.add(new JLabel("Tên màn:"));
        txtLevelName = new JTextField(levelName, 12);
        topPanel.add(txtLevelName);

        topPanel.add(new JLabel("Sun khởi đầu:"));
        spinInitialSun = new JSpinner(new SpinnerNumberModel(initialSun, 0, 9999, 25));
        topPanel.add(spinInitialSun);

        topPanel.add(new JSeparator(JSeparator.VERTICAL));

        topPanel.add(new JLabel("Cột:"));
        spinCols = new JSpinner(new SpinnerNumberModel(cols, 4, 32, 1));
        topPanel.add(spinCols);

        topPanel.add(new JLabel("Hàng:"));
        spinRows = new JSpinner(new SpinnerNumberModel(rows, 4, 24, 1));
        topPanel.add(spinRows);

        JButton btnResize = new JButton("Áp dụng Size Map");
        btnResize.setBackground(new Color(70, 130, 180));
        btnResize.setForeground(Color.WHITE);
        btnResize.addActionListener(e -> resizeMap());
        topPanel.add(btnResize);

        topPanel.add(new JSeparator(JSeparator.VERTICAL));

        JButton btnOpen = new JButton("📂 Mở File JSON...");
        btnOpen.addActionListener(e -> loadLevelFromFile());
        topPanel.add(btnOpen);

        JButton btnSave = new JButton("💾 Lưu File JSON...");
        btnSave.setBackground(new Color(34, 139, 34));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> saveLevelToFile());
        topPanel.add(btnSave);

        add(topPanel, BorderLayout.NORTH);

        // 2. CENTER: BẢN ĐỒ CANVAS (Vẽ lưới & Waypoint)
        mapCanvas = new MapCanvas();
        JScrollPane canvasScroll = new JScrollPane(mapCanvas);
        canvasScroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(canvasScroll, BorderLayout.CENTER);

        // 3. RIGHT PANEL: TABS ĐIỀU KHIỂN (Cọ vẽ, Chọn Cây, Thiết kế Wave)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setPreferredSize(new Dimension(420, 700));

        // TAB 1: TÔ MÀU BẢN ĐỒ
        tabbedPane.addTab("🎨 Tô Màu Map", buildPaintTab());

        // TAB 2: CHỌN CÂY (ALLOWED PLANTS)
        tabbedPane.addTab("🌱 Chọn Cây Cho Phép", buildPlantsTab());

        // TAB 3: THIẾT KẾ WAVE & WAYPOINTS
        tabbedPane.addTab("🧟 Thiết Kế Wave & Đường Đi", buildWavesTab());

        add(tabbedPane, BorderLayout.EAST);

        // 4. BOTTOM STATUS BAR
        lblStatus = new JLabel(" Sẵn sàng. Nhấp hoặc kéo chuột trên bản đồ để vẽ.");
        lblStatus.setPreferredSize(new Dimension(1000, 25));
        lblStatus.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        add(lblStatus, BorderLayout.SOUTH);

        refreshWaveUI();
    }

    // --- TAB 1: BẢNG CỌ TÔ MÀU ---
    private JPanel buildPaintTab() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("CHỌN CỌ TÔ MÀU (BRUSH)");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        p.add(title);
        p.add(Box.createVerticalStrut(10));

        ButtonGroup bg = new ButtonGroup();
        JRadioButton rbGrass = new JRadioButton("0. Cỏ (Grass - Màu Xanh Lá)", currentBrush == 0);
        JRadioButton rbPath = new JRadioButton("1. Đường Đi (Path - Màu Đất Nâu)", currentBrush == 1);
        JRadioButton rbPot = new JRadioButton("2. Bệ/Chậu Cây (Pot - Màu Vàng Xám)", currentBrush == 2);

        rbGrass.addActionListener(e -> { currentBrush = 0; setMode(EditMode.PAINT_TILE); });
        rbPath.addActionListener(e -> { currentBrush = 1; setMode(EditMode.PAINT_TILE); });
        rbPot.addActionListener(e -> { currentBrush = 2; setMode(EditMode.PAINT_TILE); });

        bg.add(rbGrass); bg.add(rbPath); bg.add(rbPot);
        p.add(rbGrass); p.add(rbPath); p.add(rbPot);

        p.add(Box.createVerticalStrut(20));
        JLabel tip = new JLabel("<html><b>Mẹo vẽ:</b><br>- Giữ chuột trái và rê chuột (Drag) để vẽ hàng loạt ô.<br>- Chuột phải vào ô bất kỳ để xóa về Cỏ (0).</html>");
        p.add(tip);

        p.add(Box.createVerticalStrut(20));
        JButton btnFillAll = new JButton("Tô toàn bộ Map bằng cọ hiện tại");
        btnFillAll.addActionListener(e -> {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    mapData[r][c] = currentBrush;
                }
            }
            mapCanvas.repaint();
        });
        p.add(btnFillAll);

        p.add(Box.createVerticalStrut(10));
        JButton btnClearAll = new JButton("Xóa trắng toàn bộ (Về Cỏ 0)");
        btnClearAll.addActionListener(e -> {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    mapData[r][c] = 0;
                }
            }
            mapCanvas.repaint();
        });
        p.add(btnClearAll);

        p.add(Box.createVerticalGlue());
        return p;
    }

    // --- TAB 2: CHỌN CÂY CHO PHÉP ---
    private JPanel buildPlantsTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel checkPanel = new JPanel();
        checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("CÁC LOẠI CÂY ĐƯỢC PHÉP DÙNG:");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        checkPanel.add(title);
        checkPanel.add(Box.createVerticalStrut(10));

        plantCheckBoxes = new JCheckBox[PLANT_NAMES.length];
        for (int i = 0; i < PLANT_NAMES.length; i++) {
            plantCheckBoxes[i] = new JCheckBox(PLANT_NAMES[i]);
            // Mặc định chọn 1, 2, 4 (Sunflower, Peashooter, WallNut)
            if (i == 0 || i == 1 || i == 3) plantCheckBoxes[i].setSelected(true);
            checkPanel.add(plantCheckBoxes[i]);
            checkPanel.add(Box.createVerticalStrut(4));
        }

        p.add(new JScrollPane(checkPanel), BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnSelectAll = new JButton("Chọn Tất Cả");
        btnSelectAll.addActionListener(e -> { for (JCheckBox cb : plantCheckBoxes) cb.setSelected(true); });
        JButton btnDeselectAll = new JButton("Bỏ Chọn Hết");
        btnDeselectAll.addActionListener(e -> { for (JCheckBox cb : plantCheckBoxes) cb.setSelected(false); });
        btnBar.add(btnSelectAll); btnBar.add(btnDeselectAll);
        p.add(btnBar, BorderLayout.SOUTH);

        return p;
    }

    // --- TAB 3: THIẾT KẾ WAVE & WAYPOINTS ---
    private JPanel buildWavesTab() {
        JPanel main = new JPanel(new BorderLayout(5, 5));
        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // PHẦN TRÊN: DANH SÁCH WAVES
        JPanel wavePanel = new JPanel(new BorderLayout(5, 5));
        wavePanel.setBorder(new TitledBorder("1. Danh Sách Waves"));
        waveListModel = new DefaultListModel<>();
        waveJList = new JList<>(waveListModel);
        waveJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        waveJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && waveJList.getSelectedIndex() >= 0) {
                selectedWaveIndex = waveJList.getSelectedIndex();
                loadSelectedWaveData();
            }
        });
        wavePanel.add(new JScrollPane(waveJList), BorderLayout.CENTER);

        JPanel waveBtnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JButton btnAddWave = new JButton("+ Thêm Wave");
        btnAddWave.addActionListener(e -> addWave());
        JButton btnRemoveWave = new JButton("- Xóa Wave");
        btnRemoveWave.addActionListener(e -> removeWave());
        waveBtnBar.add(btnAddWave); waveBtnBar.add(btnRemoveWave);

        waveBtnBar.add(new JLabel("Delay:"));
        spinDelayBefore = new JSpinner(new SpinnerNumberModel(300, 0, 3600, 60));
        spinDelayBefore.addChangeListener(e -> {
            WaveData w = getSelectedWave();
            if (w != null) w.delayBefore = (int) spinDelayBefore.getValue();
        });
        waveBtnBar.add(spinDelayBefore);
        waveBtnBar.add(new JLabel("frames"));
        wavePanel.add(waveBtnBar, BorderLayout.SOUTH);
        wavePanel.setPreferredSize(new Dimension(380, 160));

        // PHẦN GIỮA: CÁC SPAWNERS TRONG WAVE
        JPanel spawnerPanel = new JPanel(new BorderLayout(5, 5));
        spawnerPanel.setBorder(new TitledBorder("2. Cửa Quái (Spawners) Trong Wave Này"));

        spawnerListModel = new DefaultListModel<>();
        spawnerJList = new JList<>(spawnerListModel);
        spawnerJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        spawnerJList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && spawnerJList.getSelectedIndex() >= 0) {
                selectedSpawnerIndex = spawnerJList.getSelectedIndex();
                loadSelectedSpawnerData();
            }
        });
        spawnerPanel.add(new JScrollPane(spawnerJList), BorderLayout.CENTER);

        JPanel spawnerControls = new JPanel();
        spawnerControls.setLayout(new BoxLayout(spawnerControls, BoxLayout.Y_AXIS));

        JPanel spBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JButton btnAddSp = new JButton("+ Thêm Cửa (Spawner)");
        btnAddSp.addActionListener(e -> addSpawner());
        JButton btnRemoveSp = new JButton("- Xóa Cửa");
        btnRemoveSp.addActionListener(e -> removeSpawner());
        spBtnRow.add(btnAddSp); spBtnRow.add(btnRemoveSp);
        spBtnRow.add(new JLabel("Interval:"));
        spinSpawnInterval = new JSpinner(new SpinnerNumberModel(120, 10, 1200, 30));
        spinSpawnInterval.addChangeListener(e -> {
            SpawnerData sd = getSelectedSpawner();
            if (sd != null) sd.spawnInterval = (int) spinSpawnInterval.getValue();
        });
        spBtnRow.add(spinSpawnInterval);
        spawnerControls.add(spBtnRow);

        JPanel zRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        zRow.add(new JLabel("DS Quái (IDs):"));
        txtZombies = new JTextField("1, 1, 2", 12);
        txtZombies.addActionListener(e -> updateZombiesFromText());
        zRow.add(txtZombies);
        JButton btnApplyZ = new JButton("Cập Nhật Quái");
        btnApplyZ.addActionListener(e -> updateZombiesFromText());
        zRow.add(btnApplyZ);
        spawnerControls.add(zRow);

        JPanel zQuickBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JButton btnAddZ1 = new JButton("+ Zombie 1 (Đỏ)");
        btnAddZ1.addActionListener(e -> appendZombieId(1));
        JButton btnAddZ2 = new JButton("+ Zombie 2 (Bắn)");
        btnAddZ2.addActionListener(e -> appendZombieId(2));
        JButton btnClearZ = new JButton("Xóa Hết Quái");
        btnClearZ.addActionListener(e -> clearZombies());
        zQuickBtns.add(btnAddZ1); zQuickBtns.add(btnAddZ2); zQuickBtns.add(btnClearZ);
        spawnerControls.add(zQuickBtns);

        spawnerPanel.add(spawnerControls, BorderLayout.SOUTH);

        // PHẦN DƯỚI: WAYPOINTS CHO SPAWNER
        JPanel wpPanel = new JPanel(new BorderLayout(5, 5));
        wpPanel.setBorder(new TitledBorder("3. Lộ Trình Waypoints (Cột/Hàng)"));
        waypointListModel = new DefaultListModel<>();
        waypointJList = new JList<>(waypointListModel);
        wpPanel.add(new JScrollPane(waypointJList), BorderLayout.CENTER);

        JPanel wpControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        btnToggleWaypointMode = new JToggleButton("📍 Bật Chế Độ Click Đặt Waypoint");
        btnToggleWaypointMode.addActionListener(e -> {
            if (btnToggleWaypointMode.isSelected()) {
                setMode(EditMode.PLACE_WAYPOINT);
                btnToggleWaypointMode.setBackground(new Color(255, 140, 0));
                btnToggleWaypointMode.setForeground(Color.WHITE);
            } else {
                setMode(EditMode.PAINT_TILE);
                btnToggleWaypointMode.setBackground(null);
                btnToggleWaypointMode.setForeground(null);
            }
        });
        wpControls.add(btnToggleWaypointMode);

        JButton btnClearWp = new JButton("Xóa Hết Waypoints");
        btnClearWp.addActionListener(e -> {
            SpawnerData sd = getSelectedSpawner();
            if (sd != null) {
                sd.waypoints = new Waypoint[0];
                refreshWaypointList();
                mapCanvas.repaint();
            }
        });
        wpControls.add(btnClearWp);
        wpPanel.add(wpControls, BorderLayout.SOUTH);
        wpPanel.setPreferredSize(new Dimension(380, 170));

        JPanel container = new JPanel(new BorderLayout(5, 5));
        container.add(wavePanel, BorderLayout.NORTH);
        container.add(spawnerPanel, BorderLayout.CENTER);
        container.add(wpPanel, BorderLayout.SOUTH);

        main.add(container, BorderLayout.CENTER);
        return main;
    }

    private void setMode(EditMode mode) {
        this.currentMode = mode;
        if (mode == EditMode.PAINT_TILE) {
            btnToggleWaypointMode.setSelected(false);
            btnToggleWaypointMode.setBackground(null);
            btnToggleWaypointMode.setForeground(null);
            lblStatus.setText(" Chế độ: TÔ MÀU MAP. Đang chọn cọ: " + getBrushName(currentBrush));
        } else {
            lblStatus.setText(" Chế độ: 📍 ĐẶT WAYPOINT. Hãy click chuột lên các ô trên Map để cắm cờ lộ trình.");
        }
        mapCanvas.repaint();
    }

    private String getBrushName(int b) {
        if (b == 0) return "Cỏ (0)";
        if (b == 1) return "Đường Đi (1)";
        return "Bệ Cây (2)";
    }

    // --- LOGIC MAP RESIZE ---
    private void resizeMap() {
        int newCols = (int) spinCols.getValue();
        int newRows = (int) spinRows.getValue();
        if (newCols == cols && newRows == rows) return;

        int[][] newMap = new int[newRows][newCols];
        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if (r < rows && c < cols) {
                    newMap[r][c] = mapData[r][c];
                } else {
                    newMap[r][c] = 0; // Mở rộng thì phủ cỏ
                }
            }
        }
        this.cols = newCols;
        this.rows = newRows;
        this.mapData = newMap;
        mapCanvas.updateSize();
        mapCanvas.repaint();
        lblStatus.setText(" Đã cập nhật kích thước Map: " + cols + " cột x " + rows + " hàng.");
    }

    // --- LOGIC WAVE & SPAWNER CONTROLS ---
    private void refreshWaveUI() {
        waveListModel.clear();
        for (int i = 0; i < wavesList.size(); i++) {
            WaveData w = wavesList.get(i);
            int spCount = (w.spawners != null) ? w.spawners.length : 0;
            waveListModel.addElement("Wave " + (i + 1) + " (" + spCount + " cửa, delay: " + w.delayBefore + ")");
        }
        if (selectedWaveIndex >= 0 && selectedWaveIndex < wavesList.size()) {
            waveJList.setSelectedIndex(selectedWaveIndex);
        } else if (!wavesList.isEmpty()) {
            waveJList.setSelectedIndex(0);
        }
    }

    private WaveData getSelectedWave() {
        if (selectedWaveIndex >= 0 && selectedWaveIndex < wavesList.size()) {
            return wavesList.get(selectedWaveIndex);
        }
        return null;
    }

    private SpawnerData getSelectedSpawner() {
        WaveData w = getSelectedWave();
        if (w != null && w.spawners != null && selectedSpawnerIndex >= 0 && selectedSpawnerIndex < w.spawners.length) {
            return w.spawners[selectedSpawnerIndex];
        }
        return null;
    }

    private void loadSelectedWaveData() {
        WaveData w = getSelectedWave();
        if (w == null) return;
        spinDelayBefore.setValue(w.delayBefore);

        spawnerListModel.clear();
        if (w.spawners != null) {
            for (int i = 0; i < w.spawners.length; i++) {
                SpawnerData sd = w.spawners[i];
                int zCount = (sd.zombies != null) ? sd.zombies.length : 0;
                int wpCount = (sd.waypoints != null) ? sd.waypoints.length : 0;
                spawnerListModel.addElement("Cửa " + (i + 1) + ": " + zCount + " quái | " + wpCount + " mốc đường");
            }
        }

        if (selectedSpawnerIndex >= 0 && w.spawners != null && selectedSpawnerIndex < w.spawners.length) {
            spawnerJList.setSelectedIndex(selectedSpawnerIndex);
        } else if (w.spawners != null && w.spawners.length > 0) {
            spawnerJList.setSelectedIndex(0);
        } else {
            selectedSpawnerIndex = -1;
            loadSelectedSpawnerData();
        }
    }

    private void loadSelectedSpawnerData() {
        SpawnerData sd = getSelectedSpawner();
        if (sd == null) {
            txtZombies.setText("");
            waypointListModel.clear();
            return;
        }
        spinSpawnInterval.setValue(sd.spawnInterval);

        // Hiển thị zombies
        StringBuilder sb = new StringBuilder();
        if (sd.zombies != null) {
            for (int i = 0; i < sd.zombies.length; i++) {
                sb.append(sd.zombies[i]);
                if (i < sd.zombies.length - 1) sb.append(", ");
            }
        }
        txtZombies.setText(sb.toString());

        refreshWaypointList();
        mapCanvas.repaint();
    }

    private void refreshWaypointList() {
        waypointListModel.clear();
        SpawnerData sd = getSelectedSpawner();
        if (sd != null && sd.waypoints != null) {
            for (int i = 0; i < sd.waypoints.length; i++) {
                Waypoint wp = sd.waypoints[i];
                waypointListModel.addElement("Mốc " + (i + 1) + ": [Cột " + wp.col + ", Hàng " + wp.row + "]");
            }
        }
    }

    private void addWave() {
        WaveData nw = new WaveData();
        nw.delayBefore = 300;
        SpawnerData sp = new SpawnerData();
        sp.spawnInterval = 120;
        sp.zombies = new int[]{1};
        sp.waypoints = new Waypoint[0];
        nw.spawners = new SpawnerData[]{ sp };

        wavesList.add(nw);
        selectedWaveIndex = wavesList.size() - 1;
        refreshWaveUI();
    }

    private void removeWave() {
        if (wavesList.size() <= 1) {
            JOptionPane.showMessageDialog(this, "Màn chơi phải có ít nhất 1 Wave!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedWaveIndex >= 0 && selectedWaveIndex < wavesList.size()) {
            wavesList.remove(selectedWaveIndex);
            selectedWaveIndex = Math.max(0, selectedWaveIndex - 1);
            refreshWaveUI();
        }
    }

    private void addSpawner() {
        WaveData w = getSelectedWave();
        if (w == null) return;
        SpawnerData sp = new SpawnerData();
        sp.spawnInterval = 120;
        sp.zombies = new int[]{1};
        sp.waypoints = new Waypoint[0];

        int oldLen = (w.spawners != null) ? w.spawners.length : 0;
        SpawnerData[] newSpawners = new SpawnerData[oldLen + 1];
        for (int i = 0; i < oldLen; i++) newSpawners[i] = w.spawners[i];
        newSpawners[oldLen] = sp;
        w.spawners = newSpawners;

        selectedSpawnerIndex = oldLen;
        loadSelectedWaveData();
    }

    private void removeSpawner() {
        WaveData w = getSelectedWave();
        if (w == null || w.spawners == null || w.spawners.length <= 1) {
            JOptionPane.showMessageDialog(this, "Mỗi Wave phải có ít nhất 1 Cửa quái (Spawner)!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedSpawnerIndex >= 0 && selectedSpawnerIndex < w.spawners.length) {
            SpawnerData[] newSpawners = new SpawnerData[w.spawners.length - 1];
            int idx = 0;
            for (int i = 0; i < w.spawners.length; i++) {
                if (i != selectedSpawnerIndex) newSpawners[idx++] = w.spawners[i];
            }
            w.spawners = newSpawners;
            selectedSpawnerIndex = Math.max(0, selectedSpawnerIndex - 1);
            loadSelectedWaveData();
        }
    }

    private void updateZombiesFromText() {
        SpawnerData sd = getSelectedSpawner();
        if (sd == null) return;
        String raw = txtZombies.getText().trim();
        if (raw.isEmpty()) {
            sd.zombies = new int[0];
            loadSelectedWaveData();
            return;
        }
        String[] parts = raw.split("[,\\s]+");
        List<Integer> list = new ArrayList<>();
        for (String p : parts) {
            try {
                list.add(Integer.parseInt(p.trim()));
            } catch (Exception ignored) {}
        }
        sd.zombies = new int[list.size()];
        for (int i = 0; i < list.size(); i++) sd.zombies[i] = list.get(i);
        loadSelectedWaveData();
    }

    private void appendZombieId(int id) {
        SpawnerData sd = getSelectedSpawner();
        if (sd == null) return;
        int oldLen = (sd.zombies != null) ? sd.zombies.length : 0;
        int[] newZ = new int[oldLen + 1];
        for (int i = 0; i < oldLen; i++) newZ[i] = sd.zombies[i];
        newZ[oldLen] = id;
        sd.zombies = newZ;
        loadSelectedSpawnerData();
    }

    private void clearZombies() {
        SpawnerData sd = getSelectedSpawner();
        if (sd != null) {
            sd.zombies = new int[0];
            loadSelectedSpawnerData();
        }
    }

    // --- CANVAS HIỂN THỊ VÀ TƯƠNG TÁC BẢN ĐỒ ---
    private class MapCanvas extends JPanel {
        private static final int TILE_SIZE = 50; // Kích thước hiển thị vừa vặn trong Editor

        public MapCanvas() {
            updateSize();
            setBackground(new Color(25, 25, 30));

            MouseAdapter ma = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    handleMouse(e);
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (currentMode == EditMode.PAINT_TILE) {
                        handleMouse(e);
                    }
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        public void updateSize() {
            setPreferredSize(new Dimension(cols * TILE_SIZE + 40, rows * TILE_SIZE + 40));
            revalidate();
        }

        private void handleMouse(MouseEvent e) {
            int c = (e.getX() - 20) / TILE_SIZE;
            int r = (e.getY() - 20) / TILE_SIZE;

            if (currentMode == EditMode.PAINT_TILE) {
                if (c >= 0 && c < cols && r >= 0 && r < rows) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        mapData[r][c] = 0; // Chuột phải = Xóa về cỏ
                    } else {
                        mapData[r][c] = currentBrush;
                    }
                    repaint();
                }
            } else if (currentMode == EditMode.PLACE_WAYPOINT) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED) {
                    // Cho phép click ngoài rìa (-1 hoặc cols) để làm điểm xuất phát/kết thúc
                    SpawnerData sd = getSelectedSpawner();
                    if (sd == null) {
                        JOptionPane.showMessageDialog(LevelEditor.this, "Hãy chọn 1 Cửa quái (Spawner) trước khi đặt Waypoint!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    int oldLen = (sd.waypoints != null) ? sd.waypoints.length : 0;
                    Waypoint[] newWps = new Waypoint[oldLen + 1];
                    for (int i = 0; i < oldLen; i++) newWps[i] = sd.waypoints[i];
                    newWps[oldLen] = createWp(c, r);
                    sd.waypoints = newWps;
                    refreshWaypointList();
                    repaint();
                    lblStatus.setText(" Đã thêm Waypoint mốc " + (oldLen + 1) + ": [Cột " + c + ", Hàng " + r + "]");
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int startX = 20;
            int startY = 20;

            // 1. VẼ MA TRẬN TILES
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    int x = startX + c * TILE_SIZE;
                    int y = startY + r * TILE_SIZE;

                    int tileType = mapData[r][c];
                    if (tileType == 1) {
                        g2.setColor(new Color(210, 180, 140)); // Đường đi nâu
                        g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    } else if (tileType == 2) {
                        g2.setColor(new Color(60, 170, 60));
                        g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        g2.setColor(new Color(238, 232, 170)); // Bệ đặt cây
                        g2.fillRect(x + 6, y + 6, TILE_SIZE - 12, TILE_SIZE - 12);
                    } else {
                        g2.setColor(new Color(50, 155, 50)); // Cỏ xanh
                        g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    }

                    // Viền ô lưới
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.drawRect(x, y, TILE_SIZE, TILE_SIZE);

                    // Hiển thị tọa độ mờ
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.setFont(new Font("Arial", Font.PLAIN, 9));
                    g2.drawString(c + "," + r, x + 3, y + 12);
                }
            }

            // 2. VẼ LỘ TRÌNH WAYPOINTS CỦA SPAWNER ĐANG CHỌN
            SpawnerData sd = getSelectedSpawner();
            if (sd != null && sd.waypoints != null && sd.waypoints.length > 0) {
                g2.setStroke(new BasicStroke(3.0f));
                g2.setColor(new Color(255, 69, 0)); // Màu cam rực lửa cho đường quái đi

                for (int i = 0; i < sd.waypoints.length; i++) {
                    Waypoint wp = sd.waypoints[i];
                    int wx = startX + wp.col * TILE_SIZE + TILE_SIZE / 2;
                    int wy = startY + wp.row * TILE_SIZE + TILE_SIZE / 2;

                    // Vẽ đường nối
                    if (i < sd.waypoints.length - 1) {
                        Waypoint next = sd.waypoints[i + 1];
                        int nx = startX + next.col * TILE_SIZE + TILE_SIZE / 2;
                        int ny = startY + next.row * TILE_SIZE + TILE_SIZE / 2;
                        g2.drawLine(wx, wy, nx, ny);
                    }

                    // Vẽ cờ mốc tròn
                    g2.setColor(new Color(255, 215, 0));
                    g2.fillOval(wx - 12, wy - 12, 24, 24);
                    g2.setColor(Color.BLACK);
                    g2.drawOval(wx - 12, wy - 12, 24, 24);
                    g2.setFont(new Font("Arial", Font.BOLD, 12));
                    String lbl = String.valueOf(i + 1);
                    int sw = g2.getFontMetrics().stringWidth(lbl);
                    g2.drawString(lbl, wx - sw / 2, wy + 4);

                    g2.setColor(new Color(255, 69, 0)); // trả lại màu cam
                }
            }
        }
    }

    // --- LƯU & ĐỌC FILE JSON ---
    private void saveLevelToFile() {
        // Thu thập Allowed Plants
        List<Integer> allowedList = new ArrayList<>();
        for (int i = 0; i < plantCheckBoxes.length; i++) {
            if (plantCheckBoxes[i].isSelected()) {
                allowedList.add(i + 1); // ID 1..10
            }
        }
        if (allowedList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn phải chọn ít nhất 1 loại cây được phép dùng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LevelData data = new LevelData();
        try {
            data.id = Integer.parseInt(txtLevelId.getText().trim());
        } catch (Exception e) { data.id = 1; }

        data.name = txtLevelName.getText().trim();
        data.initialSun = (int) spinInitialSun.getValue();
        data.allowedPlants = new int[allowedList.size()];
        for (int i = 0; i < allowedList.size(); i++) data.allowedPlants[i] = allowedList.get(i);

        data.map = this.mapData;
        data.waves = wavesList.toArray(new WaveData[0]);

        // Mở hộp thoại lưu file
        JFileChooser chooser = new JFileChooser(new File("src/main/resources/data"));
        chooser.setSelectedFile(new File("level_" + data.id + ".json"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(data, writer);
                JOptionPane.showMessageDialog(this, "Đã lưu màn chơi thành công vào:\n" + file.getAbsolutePath(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                lblStatus.setText(" Đã lưu level thành công vào file: " + file.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi ghi file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void loadLevelFromFile() {
        JFileChooser chooser = new JFileChooser(new File("src/main/resources/data"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileReader reader = new FileReader(file)) {
                Gson gson = new Gson();
                LevelData data = gson.fromJson(reader, LevelData.class);

                txtLevelId.setText(String.valueOf(data.id));
                txtLevelName.setText(data.name != null ? data.name : "Custom Level");
                spinInitialSun.setValue(data.initialSun);

                // Load Map
                if (data.map != null && data.map.length > 0) {
                    this.rows = data.map.length;
                    this.cols = data.map[0].length;
                    this.mapData = data.map;
                    spinCols.setValue(this.cols);
                    spinRows.setValue(this.rows);
                    mapCanvas.updateSize();
                }

                // Load Allowed Plants
                for (JCheckBox cb : plantCheckBoxes) cb.setSelected(false);
                if (data.allowedPlants != null) {
                    for (int pId : data.allowedPlants) {
                        int idx = pId - 1;
                        if (idx >= 0 && idx < plantCheckBoxes.length) {
                            plantCheckBoxes[idx].setSelected(true);
                        }
                    }
                }

                // Load Waves
                wavesList.clear();
                if (data.waves != null) {
                    for (WaveData w : data.waves) wavesList.add(w);
                }
                selectedWaveIndex = 0;
                selectedSpawnerIndex = 0;
                refreshWaveUI();

                mapCanvas.repaint();
                JOptionPane.showMessageDialog(this, "Tải Level thành công từ: " + file.getName(), "Thành công", JOptionPane.INFORMATION_MESSAGE);
                lblStatus.setText(" Đã tải màn chơi: " + file.getName());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Lỗi khi đọc file: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LevelEditor().setVisible(true);
        });
    }
}

