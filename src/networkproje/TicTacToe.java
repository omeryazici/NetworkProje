package networkproje;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class TicTacToe implements Runnable {

    private String ip = "localhost";
    private int port = 22222;
    private Scanner scanner = new Scanner(System.in);
    private JFrame frame;
    private final int WIDTH = 506;
    private final int HEIGHT = 527;
    private Thread thread;

    private Painter painter;
    private Socket socket;
    private DataOutputStream dos;
    private DataInputStream dis;

    private ServerSocket serverSocket;

    private BufferedImage pano;
    private BufferedImage kirmiziX;
    private BufferedImage maviX;
    private BufferedImage kirmiziDaire;
    private BufferedImage maviDaire;

    private String[] bosluklar = new String[9];

    private boolean seninSiran = false;
    private boolean daire = true;
    private boolean accepted = false;
    private boolean rakipleIletisim = false;
    private boolean kazandin = false;
    private boolean rakipKazandi = false;
    private boolean beraberlik = false;

    private int boslukUzunlugu = 160;
    private int hatalar = 0;
    private int ilkNokta = -1;
    private int ikinciNokta = -1;

    private Font font = new Font("Verdana", Font.BOLD, 32);
    private Font kucukFont = new Font("Verdana", Font.BOLD, 20);
    private Font buyukFont = new Font("Verdana", Font.BOLD, 50);

    private String rakipBeklemeString = "Rakip oyuncu bekleniyor..";
    private String rakipleIletisimString = "Rakiple iletisim kurulamiyor.";
    private String kazandinString = "Kazandin!";
    private String rakipKazandiString = "Rakip Kazandi!";
    private String beraberlikString = "Oyun berabere bitti.";

    private int[][] kazananlar = new int[][]{{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};

    public TicTacToe() {
        System.out.println("Lutfen IP'yi giriniz: ");
        ip = scanner.nextLine();
        System.out.println("Lutfen port'u giriniz: ");
        port = scanner.nextInt();
        while (port < 1 || port > 65535) {
            System.out.println("Girdiginiz port geçersiz, lutfen baska bir port giriniz: ");
            port = scanner.nextInt();
        }

        imageYukle();

        painter = new Painter();
        painter.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        if (!baglan()) {
            serverBaslat();
        }

        frame = new JFrame();
        frame.setTitle("Tic-Tac-Toe");
        frame.setContentPane(painter);
        frame.setSize(WIDTH, HEIGHT);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);

        thread = new Thread(this, "TicTacToe");
        thread.start();
    }

    public void run() {
        while (true) {
            kontrolEt();
            painter.repaint();

            if (!daire && !accepted) {
                serverRequestDinleme();
            }

        }
    }

    private void yenileme(Graphics g) {
        g.drawImage(pano, 0, 0, null);
        if (rakipleIletisim) {
            g.setColor(Color.RED);
            g.setFont(kucukFont);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int stringWidth = g2.getFontMetrics().stringWidth(rakipleIletisimString);
            g.drawString(rakipleIletisimString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
            return;
        }

        if (accepted) {
            for (int i = 0; i < bosluklar.length; i++) {
                if (bosluklar[i] != null) {
                    if (bosluklar[i].equals("X")) {
                        if (daire) {
                            g.drawImage(kirmiziX, (i % 3) * boslukUzunlugu + 10 * (i % 3), (int) (i / 3) * boslukUzunlugu + 10 * (int) (i / 3), null);
                        } else {
                            g.drawImage(maviX, (i % 3) * boslukUzunlugu + 10 * (i % 3), (int) (i / 3) * boslukUzunlugu + 10 * (int) (i / 3), null);
                        }
                    } else if (bosluklar[i].equals("O")) {
                        if (daire) {
                            g.drawImage(maviDaire, (i % 3) * boslukUzunlugu + 10 * (i % 3), (int) (i / 3) * boslukUzunlugu + 10 * (int) (i / 3), null);
                        } else {
                            g.drawImage(kirmiziDaire, (i % 3) * boslukUzunlugu + 10 * (i % 3), (int) (i / 3) * boslukUzunlugu + 10 * (int) (i / 3), null);
                        }
                    }
                }
            }
            if (kazandin || rakipKazandi) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(10));
                g.setColor(Color.BLACK);
                g.drawLine(ilkNokta % 3 * boslukUzunlugu + 10 * ilkNokta % 3 + boslukUzunlugu / 2, (int) (ilkNokta / 3) * boslukUzunlugu + 10 * (int) (ilkNokta / 3) + boslukUzunlugu / 2, ikinciNokta % 3 * boslukUzunlugu + 10 * ikinciNokta % 3 + boslukUzunlugu / 2, (int) (ikinciNokta / 3) * boslukUzunlugu + 10 * (int) (ikinciNokta / 3) + boslukUzunlugu / 2);

                g.setColor(Color.RED);
                g.setFont(buyukFont);
                if (kazandin) {
                    int stringWidth = g2.getFontMetrics().stringWidth(kazandinString);
                    g.drawString(kazandinString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
                } else if (rakipKazandi) {
                    int stringWidth = g2.getFontMetrics().stringWidth(rakipKazandiString);
                    g.drawString(rakipKazandiString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
                }
            }
            if (beraberlik) {
                Graphics2D g2 = (Graphics2D) g;
                g.setColor(Color.BLACK);
                g.setFont(buyukFont);
                int stringWidth = g2.getFontMetrics().stringWidth(beraberlikString);
                g.drawString(beraberlikString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
            }
        } else {
            g.setColor(Color.RED);
            g.setFont(font);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int stringWidth = g2.getFontMetrics().stringWidth(rakipBeklemeString);
            g.drawString(rakipBeklemeString, WIDTH / 2 - stringWidth / 2, HEIGHT / 2);
        }

    }

    private void kontrolEt() {
        if (hatalar >= 10) {
            rakipleIletisim = true;
        }

        if (!seninSiran && !rakipleIletisim) {
            try {
                int space = dis.readInt();
                if (daire) {
                    bosluklar[space] = "X";
                } else {
                    bosluklar[space] = "O";
                }
                rakipKazanmaKontrol();
                beraberlikKontrol();
                seninSiran = true;
            } catch (IOException e) {
                e.printStackTrace();
                hatalar++;
            }
        }
    }

    private void kazanmaKontrol() {
        for (int i = 0; i < kazananlar.length; i++) {
            if (daire) {
                if (bosluklar[kazananlar[i][0]] == "O" && bosluklar[kazananlar[i][1]] == "O" && bosluklar[kazananlar[i][2]] == "O") {
                    ilkNokta = kazananlar[i][0];
                    ikinciNokta = kazananlar[i][2];
                    kazandin = true;
                }
            } else if (bosluklar[kazananlar[i][0]] == "X" && bosluklar[kazananlar[i][1]] == "X" && bosluklar[kazananlar[i][2]] == "X") {
                ilkNokta = kazananlar[i][0];
                ikinciNokta = kazananlar[i][2];
                kazandin = true;
            }
        }
    }

    private void rakipKazanmaKontrol() {
        for (int i = 0; i < kazananlar.length; i++) {
            if (daire) {
                if (bosluklar[kazananlar[i][0]] == "X" && bosluklar[kazananlar[i][1]] == "X" && bosluklar[kazananlar[i][2]] == "X") {
                    ilkNokta = kazananlar[i][0];
                    ikinciNokta = kazananlar[i][2];
                    rakipKazandi = true;
                }
            } else if (bosluklar[kazananlar[i][0]] == "O" && bosluklar[kazananlar[i][1]] == "O" && bosluklar[kazananlar[i][2]] == "O") {
                ilkNokta = kazananlar[i][0];
                ikinciNokta = kazananlar[i][2];
                rakipKazandi = true;
            }
        }
    }

    private void beraberlikKontrol() {
        for (int i = 0; i < bosluklar.length; i++) {
            if (bosluklar[i] == null) {
                return;
            }
        }
        beraberlik = true;
    }

    private void serverRequestDinleme() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            accepted = true;
            System.out.println("CLIENT KATILDI VE KABUL EDILDI");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean baglan() {
        try {
            socket = new Socket(ip, port);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            accepted = true;
        } catch (IOException e) {
            System.out.println("Adrese baglanilamiyor: " + ip + ":" + port + " | Server baslatiliyor..");
            return false;
        }
        System.out.println("Server'a basariyla baglandi.");
        return true;
    }

    private void serverBaslat() {
        try {
            serverSocket = new ServerSocket(port, 8, InetAddress.getByName(ip));
        } catch (Exception e) {
            e.printStackTrace();
        }
        seninSiran = true;
        daire = false;
    }

    private void imageYukle() {
        try {
            pano = ImageIO.read(getClass().getResourceAsStream("/pano.png"));
            kirmiziX = ImageIO.read(getClass().getResourceAsStream("/kırmızıX.png"));
            kirmiziDaire = ImageIO.read(getClass().getResourceAsStream("/kırmızıDaire.png"));
            maviX = ImageIO.read(getClass().getResourceAsStream("/maviX.png"));
            maviDaire = ImageIO.read(getClass().getResourceAsStream("/maviDaire.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args) {
        TicTacToe ticTacToe = new TicTacToe();
    }

    private class Painter extends JPanel implements MouseListener {

        private static final long serialVersionUID = 1L;

        public Painter() {
            setFocusable(true);
            requestFocus();
            setBackground(Color.WHITE);
            addMouseListener(this);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            yenileme(g);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (accepted) {
                if (seninSiran && !rakipleIletisim && !kazandin && !rakipKazandi) {
                    int x = e.getX() / boslukUzunlugu;
                    int y = e.getY() / boslukUzunlugu;
                    y *= 3;
                    int position = x + y;

                    if (bosluklar[position] == null) {
                        if (!daire) {
                            bosluklar[position] = "X";
                        } else {
                            bosluklar[position] = "O";
                        }
                        seninSiran = false;
                        repaint();
                        Toolkit.getDefaultToolkit().sync();

                        try {
                            dos.writeInt(position);
                            dos.flush();
                        } catch (IOException e1) {
                            hatalar++;
                            e1.printStackTrace();
                        }

                        System.out.println("VERILER GONDERILDI");
                        kazanmaKontrol();
                        beraberlikKontrol();

                    }
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

    }

}
