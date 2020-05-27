/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tank.objects.menu;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.Game;
import tank.Team;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObjectAction;
import tank.objects.Tank;
import tank.server.Client;

/**
 *
 * @author Krcma
 */
public class SocketPlayerList implements EngineMenuItem {

    private final Client client;
    private final int x, y, width, height;
    private final boolean editMode;
    private final int colW;
    private Tank selected;

    public SocketPlayerList(int _x, int _y, int _width, int _height, Client _client, boolean _editMode) {
        this.x = _x;
        this.y = _y;
        this.width = _width;
        this.height = _height;
        this.client = _client;
        this.colW = (int) (width * 0.8f / 3f);
        this.editMode = _editMode;
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        //draw colmns + they titles
        g2.setFont(Game.ttf36);
        g2.setColor(Color.white);
        g2.drawString(
                "Team A",
                this.x + 5,
                this.y + Game.ttf36.getSize() + 5
        );
        g2.drawString(
                "Team B",
                this.x + (this.width - this.colW) / 2,
                this.y + Game.ttf36.getSize() + 5
        );
        g2.drawString(
                "Not included",
                this.x + this.width - this.colW,
                this.y + Game.ttf36.getSize() + 5
        );
        g2.setColor(new Color(50, 50, 50, 100));
        g2.fillRect(this.x, this.y + 45, this.colW, this.height);
        g2.fillRect(this.x + this.width / 2 - this.colW / 2, this.y + 45, this.colW, this.height);
        g2.fillRect(this.x + this.width - this.colW, this.y + 45, this.colW, this.height);
        //draw players in list
        int a = 0, b = 0, c = 0;
        //select list of player
        List<Tank> list = getPlayers();
        for (int i = 0; i < list.size(); i++) {
            Tank t = list.get(i);
            BufferedImage image = t.getTankType().getImage();
            if (null != t.getTeam()) {
                //selected
                if (this.selected != null) {
                    if (this.selected.getID() == t.getID()) {
                        g2.setColor(new Color(240, 240, 120, 210));
                    } else {
                        g2.setColor(new Color(200, 200, 230, 200));
                    }
                } else {
                    g2.setColor(new Color(200, 200, 230, 200));
                }
                //render player in list
                switch (t.getTeam()) {
                    case A:
                        g2.drawImage(
                                image,
                                10 + this.x,
                                50 + this.y + (image.getHeight() + 5) * a, null
                        );
                        g2.setFont(Game.ttf15);
                        g2.drawString(
                                t.getTankType().getLevel() + "",
                                (int) (5 + this.x + image.getWidth() * 0.7),
                                90 + this.y + (image.getHeight() + 5) * a
                        );
                        g2.setFont(Game.ttf26);
                        g2.drawString(
                                t.getName(),
                                (int) (10 + this.x + image.getWidth() * 0.8),
                                80 + this.y + (image.getHeight() + 5) * a
                        );
                        a++;
                        break;
                    case B:
                        g2.drawImage(
                                image,
                                (int) (10 + this.x + (this.width - this.colW) / 2),
                                50 + this.y + (image.getHeight() + 5) * b, null
                        );
                        g2.setFont(Game.ttf15);
                        g2.drawString(
                                t.getTankType().getLevel() + "",
                                (int) (5 + this.x + image.getWidth() * 0.7 + (this.width - this.colW) / 2),
                                90 + this.y + (image.getHeight() + 5) * b
                        );
                        g2.setFont(Game.ttf26);
                        g2.drawString(
                                t.getName(),
                                (int) (10 + this.x + (this.width - this.colW) / 2 + image.getWidth() * 0.8),
                                80 + this.y + (image.getHeight() + 5) * b
                        );
                        b++;
                        break;
                    case NONE:
                        g2.drawImage(
                                image,
                                (int) (10 + this.x + this.width - this.colW),
                                50 + this.y + (image.getHeight() + 5) * c, null
                        );
                        g2.setFont(Game.ttf15);
                        g2.drawString(
                                t.getTankType().getLevel() + "",
                                (int) (5 + this.x + image.getWidth() * 0.7 + this.width - this.colW),
                                90 + this.y + (image.getHeight() + 5) * c
                        );
                        g2.setFont(Game.ttf26);
                        g2.drawString(
                                t.getName(),
                                (int) (10 + this.x + this.width - this.colW + image.getWidth() * 0.8),
                                80 + this.y + (image.getHeight() + 5) * c
                        );
                        c++;
                        break;
                    default:
                        break;
                }
            }
        }
    }

    int r = 0;

    @Override
    public void refresh() {
        if (Game.ENGINE.MODE == Engine.Mode.PhysicsOUT) {
            if (r > Game.ENGINE.getRPS() / 4) {
                this.r = 0;
                if (this.client != null) {
                    try {
                        //request for players on server
                        this.client.send(new Object[]{Client.Command.GetAllPlayers});
                    } catch (IOException ex) {
                        Logger.getLogger(SocketPlayerList.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                this.r++;
            }
        }
    }

    @Override
    public void mouseAction(EngineObjectAction ea, MouseEvent e, int xOFF, int yOFF) {
        if (ea != EngineObjectAction.MousePressed || !this.editMode) {
            return;
        }
        //place mode
        try {
            if (this.selected != null) {
                if (e.getY() > 50 + this.y && e.getY() < 50 + this.y + this.height) {
                    //A
                    if (e.getX() > this.x + 5 && e.getX() < this.x + 5 + this.colW) {
                        //send request for team change [A]
                        if (Game.ENGINE.MODE == Engine.Mode.PhysicsOUT) {
                            this.client.send(new Object[]{Client.Command.ChangeTeam, this.selected.getID(), Team.A});
                        } else {
                            Game.ENGINE.getEngineObjects().stream().filter((eo) -> (eo instanceof Tank)).filter((eo) -> (eo.getID() == this.selected.getID())).forEachOrdered((eo) -> {
                                ((Tank) eo).setTeam(Team.A);
                            });
                        }
                    }
                    //B
                    if (e.getX() > this.x + this.width / 2 - this.colW / 2 && e.getX() < this.x + this.width / 2 - this.colW / 2 + this.colW) {
                        //send request for team change [B]
                        if (Game.ENGINE.MODE == Engine.Mode.PhysicsOUT) {
                            this.client.send(new Object[]{Client.Command.ChangeTeam, this.selected.getID(), Team.B});
                        } else {
                            Game.ENGINE.getEngineObjects().stream().filter((eo) -> (eo instanceof Tank)).filter((eo) -> (eo.getID() == this.selected.getID())).forEachOrdered((eo) -> {
                                ((Tank) eo).setTeam(Team.B);
                            });
                        }
                    }
                    //NONE
                    if (e.getX() > this.x + this.width - this.colW && e.getX() < this.x + this.width - this.colW + this.colW) {
                        //send request for team change [NONE]
                        if (Game.ENGINE.MODE == Engine.Mode.PhysicsOUT) {
                            this.client.send(new Object[]{Client.Command.ChangeTeam, this.selected.getID(), Team.NONE});
                        } else {
                            Game.ENGINE.getEngineObjects().stream().filter((eo) -> (eo instanceof Tank)).filter((eo) -> (eo.getID() == this.selected.getID())).forEachOrdered((eo) -> {
                                ((Tank) eo).setTeam(Team.NONE);
                            });
                        }
                    }
                }
                this.selected = null;
                return;
            }
        } catch (IOException ex) {
            Logger.getLogger(SocketPlayerList.class.getName()).log(Level.SEVERE, null, ex);
        }
        //select mode
        int a = 0, b = 0, c = 0;
        List<Tank> list = getPlayers();
        for (int i = 0; i < list.size(); i++) {
            Tank t = list.get(i);
            //calc position x, y
            int imageH = t.getTankType().getImage().getHeight();
            int x = -1000, y = -1000;
            switch (t.getTeam()) {
                case A:
                    x = this.x + 5;
                    y = 50 + this.y + (imageH + 5) * a;
                    a++;
                    break;
                case B:
                    x = this.x + this.width / 2 - this.colW / 2;
                    y = 50 + this.y + (imageH + 5) * b;
                    b++;
                    break;
                case NONE:
                    x = this.x + this.width - this.colW;
                    y = 50 + this.y + (imageH + 5) * c;
                    c++;
                    break;
            }
            //test for intersect
            if (e.getX() > x && e.getX() < x + this.colW) {
                if (e.getY() > y && e.getY() < y + imageH) {
                    this.selected = t;
                    return;
                }
            }
        }
        this.selected = null;
    }

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {

    }

    private List<Tank> getPlayers() {
        List<Tank> list = Game.ENGINE.MODE == Engine.Mode.PhysicsOUT ? this.client.getAllPlayersOnServer() : new ArrayList();
        if (Game.ENGINE.MODE == Engine.Mode.PhysicsIN) {
            Game.ENGINE.getEngineObjects().stream().filter((e) -> (e instanceof Tank)).forEachOrdered((e) -> {
                list.add((Tank) e);
            });
        }
        return list;
    }

}
