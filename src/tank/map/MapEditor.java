/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.map;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import tank.Game;
import tank.Team;
import tank.engine.EngineObject;
import tank.engine.Tools;
import tank.objects.Building;
import tank.objects.Fence;
import tank.objects.Fire;
import tank.objects.Ground;
import tank.objects.Rock;
import tank.objects.Tank;
import tank.objects.TankType;
import tank.objects.Tree;

/**
 *
 * @author Krcma
 */
public class MapEditor extends javax.swing.JFrame {

    private Map map;

    private int xOFF = 0, yOFF = 0;

    private PlayerPosition selectedPP;
    private EngineObject selectedObject;
    private EngineObject object_copy;
    private boolean ready_to_place = false;

    private int BTN;
    private Point mouse, mouse2, mouse3;    //mouse = for drag, mouse2 = last pressed position, mous3 = actual position

    private Thread thread;

    private final Object[] block_list = new Object[]{Ground.GRASS, Ground.DIRT, Ground.GRAVEL, Ground.WATER, Ground.SAND, Ground.SANDSTONE, null};

    /**
     * Creates new form MapEditor
     */
    public MapEditor() {
        initComponents();
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((size.width - this.getWidth()) / 2, (size.height - this.getHeight()) / 2);
        //thread
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //fps regulation
                double last = System.nanoTime(), ticks = 0, perTick = 1e9 / 60f;
                while (true) {
                    double now = System.nanoTime();
                    ticks += (now - last) / perTick;
                    last = now;
                    while (ticks >= 1) {
                        ticks--;
                        BufferStrategy buffer = canvas1.getBufferStrategy();
                        Graphics2D g = (Graphics2D) buffer.getDrawGraphics();
                        g.clearRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
                        if (map != null) {
                            g.setStroke(new BasicStroke(2));
                            render(g);
                            //0,0 cross
                            g.setStroke(new BasicStroke(2));
                            g.setColor(Color.BLUE);
                            g.drawLine(xOFF, yOFF - 15, xOFF, yOFF + 15);
                            g.drawLine(xOFF - 15, yOFF, xOFF + 15, yOFF);
                            g.setColor(Color.gray);
                            if (mouse2 != null) {
                                int bx = mouse3.x / (Game.BLOCK_SCALE * 16);
                                int by = mouse3.y / (Game.BLOCK_SCALE * 16);
                                g.drawRect(xOFF + bx * Game.BLOCK_SCALE * 16, yOFF + by * Game.BLOCK_SCALE * 16, Game.BLOCK_SCALE * 16, Game.BLOCK_SCALE * 16);
                            }
                            //outline
                            g.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
                            g.setColor(Color.red);
                            g.drawRect(xOFF, yOFF, map.getMapSize().width * 16 * Game.BLOCK_SCALE, map.getMapSize().height * 16 * Game.BLOCK_SCALE);
                            //object ready to place
                            if (object_copy != null && ready_to_place) {
                                object_copy.render(g, mouse3.x + xOFF - object_copy.getXY().x, mouse3.y + yOFF - object_copy.getXY().y);
                            }
                        }
                        buffer.show();
                        Game.IMAGES.refershBlockTextures();
                    }
                }
            }
        });
        this.jPanel1.setVisible(false);
    }

    public void run() {
        //set Wimdows design
        try {
            UIManager.setLookAndFeel(
                    UIManager.getInstalledLookAndFeels()[3].getClassName());
            SwingUtilities.updateComponentTreeUI(this);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
        }
        this.setVisible(true);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.jComboBox1.setRenderer(new ComboBoxRendered());
        this.canvas1.createBufferStrategy(3);
        this.canvas1.setBackground(Color.black);
        this.thread.start();
    }

    public void render(Graphics2D g) {
        //ground (grass, dirt, watter,  ...)
        if (this.map.getGround() != null) {
            int sx = -this.xOFF / (Game.BLOCK_SCALE * 16);
            sx = sx >= 0 ? sx : 0;
            int sy = -this.yOFF / (Game.BLOCK_SCALE * 16);
            sy = sy >= 0 ? sy : 0;
            int ex = (-this.xOFF + this.getWidth()) / (Game.BLOCK_SCALE * 16) + 1;
            ex = ex < this.map.getMapSize().width ? ex : this.map.getMapSize().width - 1;
            int ey = (-this.yOFF + this.getHeight()) / (Game.BLOCK_SCALE * 16) + 1;
            ey = ey < this.map.getMapSize().height ? ey : this.map.getMapSize().height - 1;
            for (int x = sx; x <= ex; x++) {
                for (int y = sy; y <= ey; y++) {
                    Ground gr = this.map.getGround()[x][y];
                    if (gr != null) {
                        g.drawImage(
                                gr.getImage(),
                                this.xOFF + x * 16 * Game.BLOCK_SCALE,
                                this.yOFF + y * 16 * Game.BLOCK_SCALE,
                                this
                        );
                    }
                }
            }
        }
        //obejct
        for (int z = 3; z >= 0; z--) {
            for (int i = 0; i < map.getObjects().size(); i++) {
                EngineObject eo = map.getObjects().get(i);
                if (eo.getZIndex() == z) {
                    eo.render(g, xOFF, yOFF);
                }
            }
        }
        //player positions
        g.setFont(g.getFont().deriveFont(16f));
        for (int i = 0; i < this.map.getPlayerPositions().size(); i++) {
            PlayerPosition pp = this.map.getPlayerPositions().get(i);
            g.setColor(Color.yellow);
            g.drawLine(
                    pp.x + this.xOFF,
                    pp.y + this.yOFF,
                    (int) (pp.x + this.xOFF - Math.cos(Math.toRadians(90 + pp.hull)) * 50 * Game.BLOCK_SCALE),
                    (int) (pp.y + this.yOFF - Math.sin(Math.toRadians(90 + pp.hull)) * 50 * Game.BLOCK_SCALE)
            );
            for (int j = -30; j <= 30; j += 60) {
                g.drawLine(
                        (int) (pp.x + this.xOFF - Math.cos(Math.toRadians(90 + pp.hull)) * 50 * Game.BLOCK_SCALE),
                        (int) (pp.y + this.yOFF - Math.sin(Math.toRadians(90 + pp.hull)) * 50 * Game.BLOCK_SCALE),
                        (int) (pp.x + this.xOFF - Math.cos(Math.toRadians(90 + pp.hull)) * 50 * Game.BLOCK_SCALE + Math.cos(Math.toRadians(90 - j + pp.hull)) * 20),
                        (int) (pp.y + this.yOFF - Math.sin(Math.toRadians(90 + pp.hull)) * 50 * Game.BLOCK_SCALE + Math.sin(Math.toRadians(90 - j + pp.hull)) * 20)
                );
            }
            g.drawString("Hull:" + pp.hull, pp.x + this.xOFF + 60, pp.y + this.yOFF + 10);
            g.drawOval(pp.x + xOFF - Game.BLOCK_SCALE * 16, pp.y + yOFF - Game.BLOCK_SCALE * 16, Game.BLOCK_SCALE * 32, Game.BLOCK_SCALE * 32);
            g.setColor(Color.RED);
            g.drawLine(
                    pp.x + this.xOFF,
                    pp.y + this.yOFF,
                    (int) (pp.x + this.xOFF - Math.cos(Math.toRadians(90 + pp.turet)) * 30 * Game.BLOCK_SCALE),
                    (int) (pp.y + this.yOFF - Math.sin(Math.toRadians(90 + pp.turet)) * 30 * Game.BLOCK_SCALE)
            );
            for (int j = -30; j <= 30; j += 60) {
                g.drawLine(
                        (int) (pp.x + this.xOFF - Math.cos(Math.toRadians(90 + pp.turet)) * 30 * Game.BLOCK_SCALE),
                        (int) (pp.y + this.yOFF - Math.sin(Math.toRadians(90 + pp.turet)) * 30 * Game.BLOCK_SCALE),
                        (int) (pp.x + this.xOFF - Math.cos(Math.toRadians(90 + pp.turet)) * 30 * Game.BLOCK_SCALE + Math.cos(Math.toRadians(90 - j + pp.turet)) * 20),
                        (int) (pp.y + this.yOFF - Math.sin(Math.toRadians(90 + pp.turet)) * 30 * Game.BLOCK_SCALE + Math.sin(Math.toRadians(90 - j + pp.turet)) * 20)
                );
            }
            g.drawString("Turet:" + pp.turet, pp.x + this.xOFF + 60, pp.y + this.yOFF + 30);
        }
        if (selectedPP != null) {
            g.setColor(Color.blue);
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g.drawRect(selectedPP.x + xOFF - 25 * Game.BLOCK_SCALE, selectedPP.y + yOFF - 25 * Game.BLOCK_SCALE, 50 * Game.BLOCK_SCALE, 50 * Game.BLOCK_SCALE);
        }
        if (selectedObject != null) {
            g.setColor(Color.blue);
            g.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            Polygon p = selectedObject.getModel();
            if (p != null) {
                g.drawPolygon(
                        new int[]{p.xpoints[0] + xOFF, p.xpoints[1] + xOFF, p.xpoints[2] + xOFF, p.xpoints[3] + xOFF},
                        new int[]{p.ypoints[0] + yOFF, p.ypoints[1] + yOFF, p.ypoints[2] + yOFF, p.ypoints[3] + yOFF},
                        4
                );
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItemPlayer = new javax.swing.JMenuItem();
        jMenuItemTank = new javax.swing.JMenuItem();
        jMenuItemTree = new javax.swing.JMenuItem();
        jMenuItemRock = new javax.swing.JMenuItem();
        jMenuItemFire = new javax.swing.JMenuItem();
        jMenuItemFence = new javax.swing.JMenuItem();
        jMenuItemBuilding = new javax.swing.JMenuItem();
        jPanel3 = new javax.swing.JPanel();
        canvas1 = new java.awt.Canvas();
        jPanel4 = new javax.swing.JPanel();
        jComboBox1 = new javax.swing.JComboBox<>(this.block_list);
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();

        jMenuItemPlayer.setText("Add Player postion");
        jMenuItemPlayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemPlayerActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemPlayer);

        jMenuItemTank.setText("Add Tank");
        jMenuItemTank.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTankActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemTank);

        jMenuItemTree.setText("Add Tree");
        jMenuItemTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemTreeActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemTree);

        jMenuItemRock.setText("Add Rock");
        jMenuItemRock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemRockActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemRock);

        jMenuItemFire.setText("Add Fire");
        jMenuItemFire.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFireActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemFire);

        jMenuItemFence.setText("Add Fence");
        jMenuItemFence.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemFenceActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemFence);

        jMenuItemBuilding.setText("Add Building");
        jMenuItemBuilding.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemBuildingActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemBuilding);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Map Editor");

        canvas1.setMinimumSize(new java.awt.Dimension(200, 200));
        canvas1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                canvas1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                canvas1MouseReleased(evt);
            }
        });
        canvas1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                canvas1MouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                canvas1MouseMoved(evt);
            }
        });
        canvas1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                canvas1MouseWheelMoved(evt);
            }
        });
        canvas1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                canvas1KeyPressed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Block:");

        jLabel2.setText("Cursor:");

        jLabel3.setText("0,0");

        jLabel4.setText("Size:");

        jLabel5.setText("Name:");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Object"));

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel6.setText("X:");

        jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel7.setText("Y:");

        jTextField3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        jLabel8.setText("Hull:");

        jLabel9.setText("Turet:");

        jTextField4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField4))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel10.setText("Block:");

        jLabel11.setText("0,0");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator1)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel11)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 4, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addGap(5, 5, 5)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(canvas1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jMenu1.setText("File");

        jMenuItem4.setText("New");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem4);

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("Save");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("Open");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem3.setText("Clear");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenuItem5.setText("Fill");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.toString().endsWith(".map") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Map";
            }
        });
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                FileInputStream f = new FileInputStream(fileChooser.getSelectedFile());
                ObjectInputStream o = new ObjectInputStream(f);
                Data d = (Data) o.readObject();
                this.map = new Map("", new Dimension(1, 1));
                this.map.setData(d);
                this.jLabel5.setText("Name: " + this.map.getName());
                this.jLabel4.setText("Size: " + this.map.getMapSize().width + "," + this.map.getMapSize().height + " [BLOCK]");
                o.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    long l = 0;

    private void canvas1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvas1MousePressed
        if (this.map == null) {
            return;
        }
        this.mouse2 = new Point(evt.getX() - this.xOFF, evt.getY() - this.yOFF);
        this.BTN = evt.getButton();
        this.selectedObject = null;
        this.selectedPP = null;
        this.jPanel1.setVisible(false);
        if (evt.getButton() == MouseEvent.BUTTON1) {
            //left
            if (this.ready_to_place) {
                EngineObject eo = Tools.createEngineObjectFromConfig(this.object_copy.getConfig());
                if (eo instanceof Fence) {
                    int bx = (evt.getX() - xOFF) / (Game.BLOCK_SCALE * 16);
                    int by = (evt.getY() - yOFF) / (Game.BLOCK_SCALE * 16);
                    eo.setXY(
                            bx * (Game.BLOCK_SCALE * 16),
                            by * (Game.BLOCK_SCALE * 16)
                    );
                } else {
                    eo.setXY(evt.getX() - xOFF, evt.getY() - yOFF);
                }
                this.map.getObjects().add(eo);
                this.ready_to_place = false;
            } else {
                placeBlock(evt.getX(), evt.getY());
            }
        } else if (evt.getButton() == MouseEvent.BUTTON3) {
            //right
            for (PlayerPosition pp : this.map.getPlayerPositions()) {
                if (Math.sqrt(Math.pow(pp.x + this.xOFF - evt.getX(), 2) + Math.pow(pp.y + this.yOFF - evt.getY(), 2)) < Game.BLOCK_SCALE * 16) {
                    this.selectedPP = pp;
                    this.jPanel1.setVisible(true);
                    this.jPanel1.revalidate();
                    this.jTextField1.setText(this.selectedPP.x + "");
                    this.jTextField2.setText(this.selectedPP.y + "");
                    this.jTextField3.setVisible(true);
                    this.jTextField4.setVisible(true);
                    this.jLabel8.setVisible(true);
                    this.jLabel9.setVisible(true);
                    this.jTextField3.setText(this.selectedPP.hull + "");
                    this.jTextField4.setText(this.selectedPP.turet + "");
                    return;
                }
            }
            for (int i = 0; i < this.map.getObjects().size(); i++) {
                EngineObject eo = this.map.getObjects().get(i);
                if (eo.getModel().intersects(evt.getX() - xOFF, evt.getY() - yOFF, 2, 2)) {
                    this.selectedObject = eo;
                    this.jPanel1.setVisible(true);
                    this.jPanel1.revalidate();
                    this.jTextField1.setText(this.selectedObject.getXY().x + "");
                    this.jTextField2.setText(this.selectedObject.getXY().y + "");
                    if (this.selectedObject instanceof Tank) {
                        this.jTextField3.setVisible(true);
                        this.jTextField4.setVisible(true);
                        this.jLabel8.setVisible(true);
                        this.jLabel9.setVisible(true);
                        this.jTextField3.setText(((Tank) this.selectedObject).getHullAngle() + "");
                        this.jTextField4.setText(((Tank) this.selectedObject).getTuretAngle() + "");
                    } else {
                        this.jTextField3.setVisible(false);
                        this.jTextField4.setVisible(false);
                        this.jLabel8.setVisible(false);
                        this.jLabel9.setVisible(false);
                    }
                    return;
                }
            }
            if (System.nanoTime() - l < 0.2e9) {
                this.jPopupMenu1.show(this.canvas1, evt.getX(), evt.getY());
            }
            l = System.nanoTime();
        }
    }//GEN-LAST:event_canvas1MousePressed

    private void canvas1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvas1MouseDragged
        if (this.map == null) {
            return;
        }
        this.mouse3 = new Point(evt.getX() - this.xOFF, evt.getY() - this.yOFF);
        if (this.BTN == MouseEvent.BUTTON1) {
            //left
            placeBlock(evt.getX(), evt.getY());
        } else if (this.BTN == MouseEvent.BUTTON3) {
            //right
            if (this.mouse != null) {
                if (this.selectedPP != null) {
                    this.selectedPP.x += evt.getX() - mouse.x;
                    this.selectedPP.y += evt.getY() - mouse.y;
                } else if (this.selectedObject != null) {
                    if (!(this.selectedObject instanceof Fence)) {
                        this.selectedObject.setXY(
                                this.selectedObject.getXY().x + evt.getX() - mouse.x,
                                this.selectedObject.getXY().y + evt.getY() - mouse.y
                        );
                    }
                } else {
                    this.xOFF += evt.getX() - mouse.x;
                    this.yOFF += evt.getY() - mouse.y;
                }
            }
            this.mouse = evt.getPoint();
        }
    }//GEN-LAST:event_canvas1MouseDragged

    private void canvas1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvas1MouseReleased
        if (this.map == null) {
            return;
        }
        this.mouse = null;
        if (this.selectedPP != null) {
            this.jTextField1.setText(this.selectedPP.x + "");
            this.jTextField2.setText(this.selectedPP.y + "");
            this.jTextField3.setText(this.selectedPP.hull + "");
            this.jTextField4.setText(this.selectedPP.turet + "");
        } else if (this.selectedObject != null) {
            this.jTextField1.setText(this.selectedObject.getXY().x + "");
            this.jTextField2.setText(this.selectedObject.getXY().y + "");
            if (this.selectedObject instanceof Tank) {
                this.jTextField3.setText(((Tank) this.selectedObject).getHullAngle() + "");
                this.jTextField4.setText(((Tank) this.selectedObject).getTuretAngle() + "");
            }
        }
    }//GEN-LAST:event_canvas1MouseReleased

    private void canvas1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_canvas1MouseMoved
        this.mouse3 = new Point(evt.getX() - this.xOFF, evt.getY() - this.yOFF);
        this.jLabel3.setText((evt.getX() - this.xOFF) + "," + (evt.getY() - this.yOFF));
        this.jLabel11.setText((evt.getX() - this.xOFF) / (16 * Game.BLOCK_SCALE) + "," + (evt.getY() - this.yOFF) / (16 * Game.BLOCK_SCALE));
    }//GEN-LAST:event_canvas1MouseMoved

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        if (this.map != null) {
            int option = JOptionPane.showConfirmDialog(this, "Do you want to delete everything?", "Clear all", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                this.map.clear();
            }

        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        try {
            if (this.selectedPP != null) {
                if (evt.getSource() == this.jTextField1) {
                    this.selectedPP.x = Integer.parseInt(this.jTextField1.getText());
                } else if (evt.getSource() == this.jTextField2) {
                    this.selectedPP.y = Integer.parseInt(this.jTextField2.getText());
                } else if (evt.getSource() == this.jTextField3) {
                    this.selectedPP.hull = Integer.parseInt(this.jTextField3.getText());
                } else {
                    this.selectedPP.turet = Integer.parseInt(this.jTextField4.getText());
                }
            } else if (this.selectedObject != null) {
                if (evt.getSource() == this.jTextField1 || evt.getSource() == this.jTextField2) {
                    this.selectedObject.setXY(Integer.parseInt(this.jTextField1.getText()), Integer.parseInt(this.jTextField2.getText()));
                } else if (this.selectedObject instanceof Tank) {
                    if (evt.getSource() == this.jTextField3) {
                        ((Tank) this.selectedObject).setHullAngle(Integer.parseInt(this.jTextField3.getText()));
                    } else {
                        ((Tank) this.selectedObject).setTuretAngle(Integer.parseInt(this.jTextField4.getText()));
                    }
                }
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jMenuItemTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTreeActionPerformed
        try {
            JTextField xTF = new JTextField(this.mouse2.x + "");
            JTextField yTF = new JTextField(this.mouse2.y + "");
            ImageIcon img = new ImageIcon(Game.IMAGES.trees[0]);
            Object[] message = {
                "X:", xTF,
                "Y:", yTF,
                "", img
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Add new Tree", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.object_copy = new Tree(
                        Integer.parseInt(xTF.getText()),
                        Integer.parseInt(yTF.getText())
                );
                this.map.getObjects().add(this.object_copy);
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jMenuItemTreeActionPerformed

    private void jMenuItemTankActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemTankActionPerformed
        try {
            JTextField xTF = new JTextField(this.mouse2.x + "");
            JTextField yTF = new JTextField(this.mouse2.y + "");
            JTextField hTF = new JTextField("0");
            JTextField tTF = new JTextField("0");
            Object[] list = new Object[]{
                TankType.TANK1, TankType.TANK2, TankType.TANK3, TankType.TANK4, TankType.TANK5,
                TankType.TANK6, TankType.TANK7, TankType.TANK8, TankType.TANK9, TankType.TANK10,
                TankType.TANK11, TankType.TANK12, TankType.TANK13, TankType.TANK14, TankType.TANK15,
                TankType.TANK16, TankType.TANK17, TankType.TANK18, TankType.TANK19, TankType.TANK20
            };
            JComboBox tanktype = new JComboBox(list);
            tanktype.setRenderer(new ComboBoxRendered());
            JComboBox team = new JComboBox(new Object[]{Team.A, Team.B, Team.NONE});
            Object[] message = {
                "X:", xTF,
                "Y:", yTF,
                "Hull:", hTF,
                "Turet:", tTF,
                "Tank type:", tanktype,
                "Team:", team
            };
            int option = JOptionPane.showConfirmDialog(this, message, "Add new Tank", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.object_copy = new Tank(
                        Integer.parseInt(xTF.getText()),
                        Integer.parseInt(yTF.getText()),
                        Float.parseFloat(hTF.getText()),
                        Float.parseFloat(tTF.getText()),
                        (TankType) tanktype.getSelectedItem(),
                        (Team) team.getSelectedItem()
                );
                this.map.getObjects().add(this.object_copy);
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jMenuItemTankActionPerformed

    private void jMenuItemPlayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemPlayerActionPerformed
        try {
            JTextField xTF = new JTextField(this.mouse2.x + "");
            JTextField yTF = new JTextField(this.mouse2.y + "");
            JTextField hTF = new JTextField("0");
            JTextField tTF = new JTextField("0");
            JComboBox team = new JComboBox(new Object[]{Team.A, Team.B, Team.NONE});
            Object[] message = {
                "X:", xTF,
                "Y:", yTF,
                "Hull:", hTF,
                "Turet:", tTF,
                "Team:", team
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Add new Player positon", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.map.getPlayerPositions().add(
                        new PlayerPosition(
                                Integer.parseInt(xTF.getText()),
                                Integer.parseInt(yTF.getText()),
                                Float.parseFloat(hTF.getText()),
                                Float.parseFloat(tTF.getText()),
                                (Team) team.getSelectedItem()
                        )
                );
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jMenuItemPlayerActionPerformed

    private void jMenuItemRockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRockActionPerformed
        try {
            JTextField xTF = new JTextField(this.mouse2.x + "");
            JTextField yTF = new JTextField(this.mouse2.y + "");
            JTextField scale = new JTextField("1");
            Object[] arr = new Object[Game.IMAGES.rocks.length];
            int j = 0;
            for (BufferedImage i : Game.IMAGES.rocks) {
                arr[j] = i;
                j++;
            }
            JComboBox img = new JComboBox(arr);
            img.setRenderer(new ComboBoxRendered());
            Object[] message = {
                "X:", xTF,
                "Y:", yTF,
                "Scale: ", scale,
                "Type:", img
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Add new Rock", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.object_copy = new Rock(
                        Integer.parseInt(xTF.getText()),
                        Integer.parseInt(yTF.getText()),
                        img.getSelectedIndex(),
                        Integer.parseInt(scale.getText())
                );
                this.map.getObjects().add(this.object_copy);
            }
        } catch (Exception ex) {
            Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jMenuItemRockActionPerformed

    private void jMenuItemFireActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFireActionPerformed
        try {
            JTextField xTF = new JTextField(this.mouse2.x + "");
            JTextField yTF = new JTextField(this.mouse2.y + "");
            ImageIcon img = new ImageIcon(Game.IMAGES.fire[0]);
            Object[] message = {
                "X:", xTF,
                "Y:", yTF,
                "", img
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Add new Fire", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.object_copy = new Fire(
                        Integer.parseInt(xTF.getText()),
                        Integer.parseInt(yTF.getText()),
                        false
                );
                this.map.getObjects().add(this.object_copy);
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jMenuItemFireActionPerformed

    private void canvas1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_canvas1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
            if (this.selectedObject != null) {
                this.map.getObjects().remove(this.selectedObject);
                this.selectedObject = null;
            } else if (this.selectedPP != null) {
                this.map.getPlayerPositions().remove(this.selectedPP);
                this.selectedPP = null;
            }
        }
        if (evt.isControlDown()) {
            if (evt.getKeyCode() == KeyEvent.VK_V) {
                this.ready_to_place = true;
            }
        }
    }//GEN-LAST:event_canvas1KeyPressed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                this.map.getData().save(fileChooser.getSelectedFile(), this.map.getObjects());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MapEditor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void canvas1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_canvas1MouseWheelMoved
        int i = this.jComboBox1.getSelectedIndex() + evt.getWheelRotation();
        i = i < 0 ? this.jComboBox1.getItemCount() - 1 : i;
        i = i < this.jComboBox1.getItemCount() ? i : 0;
        this.jComboBox1.setSelectedIndex(i);

    }//GEN-LAST:event_canvas1MouseWheelMoved

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        try {
            JTextField name = new JTextField("map_1");
            JTextField width = new JTextField("75");
            JTextField height = new JTextField("75");
            Object[] message = {
                "Name:", name,
                "Width:", width,
                "Height:", height
            };

            int option = JOptionPane.showConfirmDialog(null, message, "New map", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.map = new Map(
                        name.getText(),
                        new Dimension(
                                Integer.parseInt(width.getText()),
                                Integer.parseInt(height.getText())
                        )
                );
                this.jLabel5.setText("Name: " + name.getText());
                this.jLabel4.setText("Size: " + this.map.getMapSize().width + "," + this.map.getMapSize().height + " [BLOCK]");
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        for (int i = 0; i < this.map.getMapSize().width; i++) {
            for (int j = 0; j < this.map.getMapSize().height; j++) {
                this.map.getData().ground[i][j] = (Ground) this.jComboBox1.getSelectedItem();
            }
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItemFenceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemFenceActionPerformed
        try {
            JTextField xTF = new JTextField(this.mouse2.x / (Game.BLOCK_SCALE * 16) + "");
            JTextField yTF = new JTextField(this.mouse2.y / (Game.BLOCK_SCALE * 16) + "");
            Object[] list = new Object[]{
                Fence.Orientation.DOWN_LEFT, Fence.Orientation.DOWN_RIFHT, Fence.Orientation.UP_LEFT,
                Fence.Orientation.UP_RIGHT, Fence.Orientation.VERTICAL, Fence.Orientation.HORISONTAL
            };
            JComboBox orient = new JComboBox(list);
            orient.setRenderer(new ComboBoxRendered());
            Object[] message = {
                "X[Block]:", xTF,
                "Y[Block]:", yTF,
                "Orientation:", orient
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Add new Fence", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.object_copy = new Fence(
                        Integer.parseInt(xTF.getText()) * (Game.BLOCK_SCALE * 16),
                        Integer.parseInt(yTF.getText()) * (Game.BLOCK_SCALE * 16),
                        (Fence.Orientation) orient.getSelectedItem()
                );
                this.map.getObjects().add(this.object_copy);
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jMenuItemFenceActionPerformed

    private void jMenuItemBuildingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemBuildingActionPerformed
        try {
            JTextField xTF = new JTextField(this.mouse2.x + "");
            JTextField yTF = new JTextField(this.mouse2.y + "");
            Object[] arr = new Object[Game.IMAGES.buildings.length];
            int j = 0;
            for (BufferedImage i : Game.IMAGES.buildings) {
                arr[j] = i;
                j++;
            }
            JComboBox img = new JComboBox(arr);
            img.setRenderer(new ComboBoxRendered());
            Object[] message = {
                "X:", xTF,
                "Y:", yTF,
                "Img:", img
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Add new Builfing", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                this.object_copy = new Building(
                        Integer.parseInt(xTF.getText()),
                        Integer.parseInt(yTF.getText()),
                        img.getSelectedIndex()
                );
                this.map.getObjects().add(this.object_copy);
            }
        } catch (Exception ex) {
        }
    }//GEN-LAST:event_jMenuItemBuildingActionPerformed

    private void placeBlock(int x, int y) {
        x -= this.xOFF;
        y -= this.yOFF;
        //left
        int xM = -1, yM = -1;
        xM = x / (Game.BLOCK_SCALE * 16);
        yM = y / (Game.BLOCK_SCALE * 16);
        if (xM >= 0 && yM >= 0 && xM < this.map.getMapSize().width && yM < this.map.getMapSize().height) {
            if (this.jComboBox1.getSelectedItem() == null) {
                this.map.getGround()[xM][yM] = null;
            } else {
                this.map.getGround()[xM][yM] = (Ground) this.jComboBox1.getSelectedItem();
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Canvas canvas1;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItemBuilding;
    private javax.swing.JMenuItem jMenuItemFence;
    private javax.swing.JMenuItem jMenuItemFire;
    private javax.swing.JMenuItem jMenuItemPlayer;
    private javax.swing.JMenuItem jMenuItemRock;
    private javax.swing.JMenuItem jMenuItemTank;
    private javax.swing.JMenuItem jMenuItemTree;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    // End of variables declaration//GEN-END:variables
}
