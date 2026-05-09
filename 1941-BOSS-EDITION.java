package completeGame1941;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class CompleteGame1941 extends JPanel implements ActionListener, KeyListener {
    private final int WIDTH = 400, HEIGHT = 600;
    private Timer timer;

    private enum State { TITLE, PLAYING, BOSS_COMING, BOSS_BATTLE, GAMEOVER, VICTORY }
    private State currentState = State.TITLE;

    // ゲーム設定
    private int difficultyLevel = 2;
    private int enemySpeed, spawnRate;
    private int playerX = 180, playerY = 500;
    private final int OBJ_SIZE = 40;
    private int score = 0, lives = 3;
    private boolean isPoweredUp = false;

    // ボス設定
    private int bossX = 100, bossY = -150;
    private int bossHp = 0, maxBossHp = 0;
    private int bossDir = 2;
    private int bossTimer = 0;

    // エンティティ
    private boolean leftPressed, rightPressed, spacePressed;
    private int fireCooldown = 0;
    private ArrayList<Rectangle> pBullets = new ArrayList<>();
    private ArrayList<Rectangle> enemies = new ArrayList<>();
    private ArrayList<Rectangle> eBullets = new ArrayList<>(); // 敵の弾
    private ArrayList<Point> stars = new ArrayList<>();
    private Random rand = new Random();

    public CompleteGame1941() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(5, 5, 25));
        setFocusable(true);
        addKeyListener(this);
        for (int i = 0; i < 50; i++) stars.add(new Point(rand.nextInt(WIDTH), rand.nextInt(HEIGHT)));
        timer = new Timer(16, this);
        timer.start();
    }

    private void resetGame() {
        switch (difficultyLevel) {
            case 1 -> { enemySpeed = 3; spawnRate = 4; lives = 5; maxBossHp = 50; }
            case 2 -> { enemySpeed = 5; spawnRate = 7; lives = 3; maxBossHp = 100; }
            case 3 -> { enemySpeed = 8; spawnRate = 12; lives = 1; maxBossHp = 200; }
        }
        playerX = 180; playerY = 500;
        score = 0; isPoweredUp = false; bossY = -150; bossHp = maxBossHp;
        pBullets.clear(); enemies.clear(); eBullets.clear();
        currentState = State.PLAYING;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 背景
        g2.setColor(Color.WHITE);
        for (Point p : stars) g2.fillOval(p.x, p.y, 2, 2);

        switch (currentState) {
            case TITLE -> drawTitle(g2);
            case PLAYING, BOSS_COMING, BOSS_BATTLE -> drawGame(g2);
            case GAMEOVER -> drawEndScreen(g2, "GAME OVER", Color.RED);
            case VICTORY -> drawEndScreen(g2, "MISSION CLEAR", Color.CYAN);
        }
    }

    private void drawTitle(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("1941: BOSS RUSH", 35, 180);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        String[] diffs = {"", "[1] EASY", "[2] NORMAL", "[3] HARD"};
        for (int i = 1; i <= 3; i++) {
            g.setColor(difficultyLevel == i ? Color.YELLOW : Color.GRAY);
            g.drawString(diffs[i], 150, 300 + i * 40);
        }
        g.setColor(Color.WHITE);
        g.drawString("PRESS SPACE TO START", 90, 500);
    }

    private void drawGame(Graphics2D g) {
        // 自機
        g.setColor(isPoweredUp ? Color.ORANGE : Color.CYAN);
        g.fillPolygon(new int[]{playerX, playerX+20, playerX+40}, new int[]{playerY+40, playerY, playerY+40}, 3);

        // 敵と弾
        g.setColor(Color.RED);
        for (Rectangle e : enemies) g.fillRoundRect(e.x, e.y, e.width, e.height, 8, 8);
        g.setColor(Color.MAGENTA);
        for (Rectangle eb : eBullets) g.fillOval(eb.x, eb.y, eb.width, eb.height);
        g.setColor(isPoweredUp ? Color.PINK : Color.YELLOW);
        for (Rectangle b : pBullets) g.fillOval(b.x, b.y, b.width, b.height);

        // ボス描画
        if (currentState == State.BOSS_BATTLE || currentState == State.BOSS_COMING) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(bossX, bossY, 200, 80);
            g.setColor(Color.RED);
            g.fillRect(bossX + 20, bossY + 70, 160, 20); // 砲台っぽく
            // ボスHPバー
            g.setColor(Color.GRAY);
            g.fillRect(100, 20, 200, 10);
            g.setColor(Color.RED);
            g.fillRect(100, 20, (int)(200 * ((double)bossHp / maxBossHp)), 10);
        }

        // WARNING表示
        if (currentState == State.BOSS_COMING && (System.currentTimeMillis() / 500) % 2 == 0) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("WARNING!", 100, 300);
        }

        // UI
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString(String.format("SCORE:%06d", score), 10, 25);
        g.setColor(Color.RED);
        g.drawString("LIFE:" + "♥".repeat(Math.max(0, lives)), 10, 50);
    }

    private void drawEndScreen(Graphics2D g, String msg, Color c) {
        g.setColor(new Color(0,0,0,200));
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(c);
        g.setFont(new Font("Arial", Font.BOLD, 45));
        g.drawString(msg, 40, 250);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("Score: " + score, 150, 320);
        g.drawString("Press [R] to Title", 120, 450);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateBackground();
        if (currentState != State.TITLE && currentState != State.GAMEOVER && currentState != State.VICTORY) updateGame();
        repaint();
    }

    private void updateBackground() {
        for (Point p : stars) {
            p.y += (currentState == State.BOSS_BATTLE) ? 1 : 3;
            if (p.y > HEIGHT) { p.y = 0; p.x = rand.nextInt(WIDTH); }
        }
    }

    private void updateGame() {
        // プレイヤー移動
        if (leftPressed && playerX > 0) playerX -= 7;
        if (rightPressed && playerX < WIDTH - OBJ_SIZE) playerX += 7;

        // 攻撃
        if (fireCooldown > 0) fireCooldown--;
        if (spacePressed && fireCooldown == 0) {
            pBullets.add(new Rectangle(playerX + 18, playerY, 4, 10));
            if (isPoweredUp) {
                pBullets.add(new Rectangle(playerX + 18, playerY, 5, 10));
                pBullets.add(new Rectangle(playerX + 18, playerY, 6, 10));
            }
            fireCooldown = 12;
        }

        // 弾の移動
        pBullets.removeIf(b -> {
            b.y -= 12;
            if (b.width == 5) b.x -= 2;
            if (b.width == 6) b.x += 2;
            return b.y < 0;
        });

        if (currentState == State.PLAYING) {
            updateNormalEnemies();
            if (score >= 3000) currentState = State.BOSS_COMING;
        } else if (currentState == State.BOSS_COMING) {
            bossY += 2;
            if (bossY >= 50) currentState = State.BOSS_BATTLE;
        } else if (currentState == State.BOSS_BATTLE) {
            updateBossBehavior();
        }

        // 当たり判定 (敵の弾 vs プレイヤー)
        eBullets.removeIf(eb -> {
            eb.y += 6;
            if (eb.intersects(new Rectangle(playerX, playerY, OBJ_SIZE, OBJ_SIZE))) {
                lives--;
                if (lives <= 0) currentState = State.GAMEOVER;
                return true;
            }
            return eb.y > HEIGHT;
        });
    }

    private void updateNormalEnemies() {
        if (rand.nextInt(100) < spawnRate) enemies.add(new Rectangle(rand.nextInt(WIDTH - 30), -30, 30, 30));
        enemies.removeIf(en -> {
            en.y += enemySpeed;
            if (en.intersects(new Rectangle(playerX, playerY, OBJ_SIZE, OBJ_SIZE))) {
                lives--;
                if (lives <= 0) currentState = State.GAMEOVER;
                return true;
            }
            return en.y > HEIGHT;
        });

        // 弾 vs 雑魚
        for (int i = 0; i < pBullets.size(); i++) {
            for (int j = 0; j < enemies.size(); j++) {
                if (pBullets.get(i).intersects(enemies.get(j))) {
                    enemies.remove(j); pBullets.remove(i--);
                    score += 100;
                    if (score == 1500) isPoweredUp = true;
                    break;
                }
            }
        }
    }

    private void updateBossBehavior() {
        // 横移動
        bossX += bossDir;
        if (bossX < 0 || bossX > WIDTH - 200) bossDir *= -1;

        // 攻撃パターン
        bossTimer++;
        if (bossTimer % 40 == 0) { // 3方向弾
            eBullets.add(new Rectangle(bossX + 100, bossY + 80, 10, 10));
            eBullets.add(new Rectangle(bossX + 40, bossY + 80, 10, 10));
            eBullets.add(new Rectangle(bossX + 160, bossY + 80, 10, 10));
        }

        // 自機の弾 vs ボス
        for (int i = 0; i < pBullets.size(); i++) {
            if (pBullets.get(i).intersects(new Rectangle(bossX, bossY, 200, 80))) {
                pBullets.remove(i--);
                bossHp--;
                score += 10;
                if (bossHp <= 0) {
                    currentState = State.VICTORY;
                    score += 5000;
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (currentState == State.TITLE) {
            if (key == KeyEvent.VK_1) difficultyLevel = 1;
            if (key == KeyEvent.VK_2) difficultyLevel = 2;
            if (key == KeyEvent.VK_3) difficultyLevel = 3;
            if (key == KeyEvent.VK_SPACE) resetGame();
        } else if (key == KeyEvent.VK_R) currentState = State.TITLE;
        
        if (key == KeyEvent.VK_LEFT) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT) rightPressed = true;
        if (key == KeyEvent.VK_SPACE) spacePressed = true;
    }

    @Override public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT) rightPressed = false;
        if (key == KeyEvent.VK_SPACE) spacePressed = false;
    }
    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame f = new JFrame("1941 BOSS EDITION");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new CompleteGame1941());
        f.pack(); f.setLocationRelativeTo(null); f.setVisible(true);
    }
}
