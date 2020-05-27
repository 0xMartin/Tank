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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import tank.Game;
import tank.engine.Engine;
import tank.engine.EngineMenuItem;
import tank.engine.EngineObjectAction;
import tank.server.Client;

/**
 *
 * @author Krcma
 */
public class Chat implements EngineMenuItem {

    private int x, y;

    private final List<ChatText> text;

    /**
     * 0 - (String) name 1 - (String) message
     *
     * @param message
     */
    public void writeMessage(Object[] message) {
        this.text.add(new ChatText((String) message[0], (String) message[1]));
    }

    private class ChatText {

        private final String text, name;
        private int time;

        public ChatText(String _name, String _text) {
            this.name = _name;
            this.text = _text;
            this.time = Game.ENGINE.getRPS() * 5;
        }

        public boolean isDeath() {
            return this.time <= 0;
        }
    }

    public Chat(int _x, int _y) {
        this.x = _x;
        this.y = _y;
        this.text = new ArrayList<>();
    }

    @Override
    public void render(Graphics2D g2, int xOFF, int yOFF) {
        g2.setFont(Game.ttf26);
        for (int i = this.text.size() - 1; i >= 0; i--) {
            ChatText ct = this.text.get(i);
            g2.setColor(Color.BLACK);
            g2.drawString(ct.name, x, y - i * 20);
            g2.setColor(Color.WHITE);
            g2.drawString(ct.text, x + g2.getFontMetrics().stringWidth(ct.name) + 10, y - i * 20);
        }
        if (this.message.length() != 0) {
            g2.setColor(Color.YELLOW);
            g2.drawString(this.message, x, y + 20);
        }
    }

    @Override
    public void refresh() {
        for (int i = 0; i < this.text.size(); i++) {
            ChatText ct = this.text.get(i);
            ct.time--;
            if (ct.isDeath()) {
                this.text.remove(i);
                i--;
            }
        }
    }

    @Override
    public void mouseAction(EngineObjectAction ea, MouseEvent e, int xOFF, int yOFF) {
        //#######################################
    }

    private boolean write = false;
    private String message = "";

    @Override
    public void keyAction(EngineObjectAction ea, KeyEvent e) {
        if (ea == EngineObjectAction.KeyPressed) {
            if (this.write) {
                this.message += e.getKeyChar();
            }
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (this.write) {
                    //send message
                    if (Game.ENGINE.MODE == Engine.Mode.PhysicsIN) {
                        
                        //server mode
                        if (Game.server != null) {
                            this.writeMessage(new Object[]{Game.ENGINE.getPlayer().getName(), this.message});
                            Game.server.writeToChat(this.message);
                        }
                    } else {
                        //send message using client socket
                        try {
                            Game.client.send(new Object[]{Client.Command.Chat, this.message});
                        } catch (IOException ex) {
                            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                this.write = !this.write;
                this.message = "";
            }
        }
    }

}
