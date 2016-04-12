package pacman;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.awt.*;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.swing.*;

public class board extends JPanel implements ActionListener {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Dimension d;
    private final Font small_font = new Font("Comic Sans MS", Font.BOLD, 16);
    private Image ii;
    private final Color dot_color = new Color(192, 192, 0);
    private Color maze_color;
    private boolean in_game = false;
    private boolean dying = false;
    private final int block_size = 24;
    private final int number_of_blocks = 15;
    private final int screen_size = number_of_blocks * block_size;
    private final int pac_anim_delay = 2;
    private final int pacman_anim_count = 4;
    private final int max_ghosts = 12;
    private final int pacman_speed = 6;
    private int pac_anim_count = pac_anim_delay;
    private int pac_anim_dir = 1;
    private int pacman_anim_pos = 0;
    private int number_of_ghosts = 6;
    private int pacs_left, score;
    private int[] dx, dy;
    private int[] ghostx, ghosty, ghostdx, ghostdy, ghost_speed;
    private Image ghost, pacman_up, pacman_left, pacman_right, pacman_down;
    private int pacmanx, pacmany, pacmandx, pacmandy;
    private int reqdx, reqdy, viewdx, viewdy;

    //Numbers 1, 2, 4 and 8 represent left, top, right and bottom corners respectively. Number 16 is a point. 
    //These number can be added, for example number 19 in the upper left corner means that the square will have top and left borders and a point (16 + 2 + 1).
    private final short leveldata[] = {
         19, 26, 26, 18, 26, 26, 22,  0, 19, 26, 26, 18, 26, 26, 22,
        21,  0,  0, 21,  0,  0, 21,  0, 21,  0,  0, 21,  0,  0, 21,
        21,  0,  0, 21,  0,  0, 21,  0, 21,  0,  0, 21,  0,  0, 21,
        17, 26, 26, 16, 26, 18, 20,  0, 17, 18, 26, 16, 26, 26, 20,
        21,  0,  0, 21,  0, 25, 24, 26, 24, 28,  0, 21,  0,  0, 21,
        21,  0,  0, 21,  0,  0,  0,  0,  0,  0,  0, 21,  0,  0, 21,
        25, 26, 26, 20,  0, 19, 22,  0, 19, 22,  0, 17, 26, 26, 28,
         0,  0,  0, 17, 26, 16, 16, 26, 16, 16, 26, 20,  0,  0,  0,
         0,  0,  0, 21,  0, 17, 20,  0, 17, 20,  0, 21,  0,  0,  0,
        19, 26, 26, 20,  0, 25, 20,  0, 17, 28,  0, 17, 26, 26, 22,
        21,  0,  0, 21,  0,  0, 21,  0, 21,  0,  0, 21,  0,  0, 21,
        25, 22,  0, 21,  0, 19, 20,  0, 17, 22,  0, 21,  0, 19, 28,
         0, 21,  0, 21,  0, 17, 28,  0, 25, 20,  0, 21,  0, 21,  0,
        19, 24, 26, 16, 26, 28,  0,  0,  0, 25, 26, 16, 26, 24, 22,
        29,  0,  0, 29,  0,  0,  0,  0,  0,  0,  0, 29,  0,  0, 29
    };

    private final int valid_speeds[] = {1, 2, 3, 4, 6, 8};
    private final int max_speed = 6;
    private int current_speed = 3;
    private short[] screen_data;
    private Timer timer;

    public board() {
        load_images();
        init();
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.black);
        setDoubleBuffered(true);
        try {
            File ff = new File("music.wav");
            AudioInputStream stream;
            AudioFormat format;
            DataLine.Info info;
            Clip clip;
            stream = AudioSystem.getAudioInputStream(ff);
            format = stream.getFormat();
            info = new DataLine.Info(Clip.class, format);
            clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clip.start();
        }
        catch (Exception e) {}
    }

    private void init() {
        screen_data = new short[number_of_blocks * number_of_blocks];
        maze_color = new Color(5, 100, 5);
        d = new Dimension(400, 400);
        ghostx = new int[max_ghosts];
        ghostdx = new int[max_ghosts];
        ghosty = new int[max_ghosts];
        ghostdy = new int[max_ghosts];
        ghost_speed = new int[max_ghosts];
        dx = new int[4];
        dy = new int[4];
        timer = new Timer(40, this);
        timer.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        init_game();
    }

    private void animation() {
        pac_anim_count--;
        if (pac_anim_count <= 0) {
            pac_anim_count = pac_anim_delay;
            pacman_anim_pos = pacman_anim_pos + pac_anim_dir;
            if (pacman_anim_pos == (pacman_anim_count - 1) || pacman_anim_pos == 0) {
                pac_anim_dir = -pac_anim_dir;
            }
        }
    }

    private void play_game(Graphics2D g2d) {
        if (dying) {
            death();
        } else {
            move_pacman();
            draw_pacman(g2d);
            move_ghosts(g2d);
            check_maze();
        }
    }

    private void show_screen(Graphics2D g2d) {
        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, screen_size / 2 - 30, screen_size - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, screen_size / 2 - 30, screen_size - 100, 50);
        String s = "Press s to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);
        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (screen_size - metr.stringWidth(s)) / 2, screen_size / 2);
    }

    private void draw_score(Graphics2D g) {
        int i;
        String s;
        g.setFont(small_font);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, screen_size / 2 + 96, screen_size + 16);
        for (i = 0; i < pacs_left; i++) {
            g.drawImage(pacman_right, i * 28 + 8, screen_size + 1, this);
        }
    }

    private void check_maze() {
        short i = 0;
        boolean finished = true;
        while (i < number_of_blocks * number_of_blocks && finished) {
            if ((screen_data[i] & 48) != 0) {
                finished = false;
            }
            i++;
        }
        if (finished) {
            score += 50;
            if (number_of_ghosts < max_ghosts) {
                number_of_ghosts++;
            }
            if (current_speed < max_speed) {
                current_speed++;
            }
            init_level();
        }
    }

    private void death() {
        pacs_left--;
        if (pacs_left == 0) {
        	in_game = false;
        	JOptionPane.showMessageDialog(this, "Your score is " + ""+score);
        	System.exit(0);
        }
        else
        continue_level();
    }

    private void move_ghosts(Graphics2D g2d) {
        short i;
        int pos;
        int count;
        for (i = 0; i < number_of_ghosts; i++) {
            if (ghostx[i] % block_size == 0 && ghosty[i] % block_size == 0) {
                pos = ghostx[i] / block_size + number_of_blocks * (int) (ghosty[i] / block_size);
                count = 0;
                if ((screen_data[pos] & 1) == 0 && ghostdx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if ((screen_data[pos] & 2) == 0 && ghostdy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }
                if ((screen_data[pos] & 4) == 0 && ghostdx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
                if ((screen_data[pos] & 8) == 0 && ghostdy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }
                if (count == 0) {
                    if ((screen_data[pos] & 15) == 15) {
                        ghostdx[i] = 0;
                        ghostdy[i] = 0;
                    } else {
                        ghostdx[i] = -ghostdx[i];
                        ghostdy[i] = -ghostdy[i];
                    }
                } else {
                    count = (int) (Math.random() * count);
                    if (count > 3) {
                        count = 3;
                    }
                    ghostdx[i] = dx[count];
                    ghostdy[i] = dy[count];
                }
            }
            ghostx[i] = ghostx[i] + (ghostdx[i] * ghost_speed[i]);
            ghosty[i] = ghosty[i] + (ghostdy[i] * ghost_speed[i]);
            draw_ghost(g2d, ghostx[i] + 1, ghosty[i] + 1);
            if (pacmanx > (ghostx[i] - 12) && pacmanx < (ghostx[i] + 12)
                    && pacmany > (ghosty[i] - 12) && pacmany < (ghosty[i] + 12)
                    && in_game) {
                dying = true;
            }
        }
    }

    private void draw_ghost(Graphics2D g2d, int x, int y) {
        g2d.drawImage(ghost, x, y, this);
    }

    private void move_pacman() {
        int pos;
        short ch;
        if (reqdx == -pacmandx && reqdy == -pacmandy) {
            pacmandx = reqdx;
            pacmandy = reqdy;
            viewdx = pacmandx;
            viewdy = pacmandy;
        }
        if (pacmanx % block_size == 0 && pacmany % block_size == 0) {
            pos = pacmanx / block_size + number_of_blocks * (int) (pacmany / block_size);
            ch = screen_data[pos];
            if ((ch & 16) != 0) {
                screen_data[pos] = (short) (ch & 15);
                score++;
            }
            if (reqdx != 0 || reqdy != 0) {
                if (!((reqdx == -1 && reqdy == 0 && (ch & 1) != 0)
                        || (reqdx == 1 && reqdy == 0 && (ch & 4) != 0)
                        || (reqdx == 0 && reqdy == -1 && (ch & 2) != 0)
                        || (reqdx == 0 && reqdy == 1 && (ch & 8) != 0))) {
                    pacmandx = reqdx;
                    pacmandy = reqdy;
                    viewdx = pacmandx;
                    viewdy = pacmandy;
                }
            }
            if ((pacmandx == -1 && pacmandy == 0 && (ch & 1) != 0)
                    || (pacmandx == 1 && pacmandy == 0 && (ch & 4) != 0)
                    || (pacmandx == 0 && pacmandy == -1 && (ch & 2) != 0)
                    || (pacmandx == 0 && pacmandy == 1 && (ch & 8) != 0)) {
                pacmandx = 0;
                pacmandy = 0;
            }
        }
        pacmanx = pacmanx + pacman_speed * pacmandx;
        pacmany = pacmany + pacman_speed * pacmandy;
    }

    private void draw_pacman(Graphics2D g2d) {
        if (viewdx == -1) {
            draw_pacnan_left(g2d);
        } else if (viewdx == 1) {
            draw_pacman_right(g2d);
        } else if (viewdy == -1) {
            draw_pacman_up(g2d);
        } else {
            draw_pacman_down(g2d);
        }
    }

    private void draw_pacman_up(Graphics2D g2d) {
    	g2d.drawImage(pacman_up, pacmanx + 1, pacmany + 1, this);
    }

    private void draw_pacman_down(Graphics2D g2d) {
        g2d.drawImage(pacman_down, pacmanx + 1, pacmany + 1, this);
    }

    private void draw_pacnan_left(Graphics2D g2d) {
        g2d.drawImage(pacman_left, pacmanx + 1, pacmany + 1, this);
    }

    private void draw_pacman_right(Graphics2D g2d) {
        g2d.drawImage(pacman_right, pacmanx + 1, pacmany + 1, this);
    }

    private void draw_maze(Graphics2D g2d) {
        short i = 0;
        int x, y;
        for (y = 0; y < screen_size; y += block_size) {
            for (x = 0; x < screen_size; x += block_size) {
                g2d.setColor(maze_color);
                g2d.setStroke(new BasicStroke(2));
                if ((screen_data[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + block_size - 1);
                }
                if ((screen_data[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + block_size - 1, y);
                }
                if ((screen_data[i] & 4) != 0) { 
                    g2d.drawLine(x + block_size - 1, y, x + block_size - 1,
                            y + block_size - 1);
                }
                if ((screen_data[i] & 8) != 0) { 
                    g2d.drawLine(x, y + block_size - 1, x + block_size - 1,
                            y + block_size - 1);
                }
                if ((screen_data[i] & 16) != 0) { 
                    g2d.setColor(dot_color);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }
                i++;
            }
        }
    }

    private void init_game() {
        pacs_left = 3;
        score = 0;
        init_level();
        number_of_ghosts = 6;
        current_speed = 3;
    }

    private void init_level() {
        int i;
        for (i = 0; i < number_of_blocks * number_of_blocks; i++) {
            screen_data[i] = leveldata[i];
        }
        continue_level();
    }

    private void continue_level() {
        short i;
        int dx = 1;
        int random;
        for (i = 0; i < number_of_ghosts; i++) {
            ghosty[i] = 4 * block_size;
            ghostx[i] = 4 * block_size;
            ghostdy[i] = 0;
            ghostdx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (current_speed + 1));
            if (random > current_speed) {
                random = current_speed;
            }
            ghost_speed[i] = valid_speeds[random];
        }
        pacmanx =  0 * block_size;
        pacmany = 14 * block_size;
        pacmandx = 0;
        pacmandy = 0;
        reqdx = 0;
        reqdy = 0;
        viewdx = -1;
        viewdy = 0;
        dying = false;
    }

    private void load_images() {
        ghost = new ImageIcon("ghost.png").getImage();
        pacman_up = new ImageIcon("up.png").getImage();
        pacman_down = new ImageIcon("down.png").getImage();
        pacman_left = new ImageIcon("left.png").getImage();
        pacman_right = new ImageIcon("right.png").getImage();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        do_drawing(g);
    }

    private void do_drawing(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);
        draw_maze(g2d);
        draw_score(g2d);
        animation();
        if (in_game) {
            play_game(g2d);
        } else {
            show_screen(g2d);
        }
        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if (in_game) {
                if (key == KeyEvent.VK_LEFT) {
                    reqdx = -1;
                    reqdy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    reqdx = 1;
                    reqdy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    reqdx = 0;
                    reqdy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    reqdx = 0;
                    reqdy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    in_game = false;
                } else if (key == KeyEvent.VK_P) {
                    if (timer.isRunning()) {
                        timer.stop();
                    } else {
                        timer.start();
                    }
                }
            } else {
                if (key == 's' || key == 'S') {
                    in_game = true;
                    init_game();
                }
            }
        }
        @Override
        public void keyReleased(KeyEvent e) {
            int key = e.getKeyCode();
            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                reqdx = 0;
                reqdy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}