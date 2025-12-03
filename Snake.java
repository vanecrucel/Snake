import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

public class Snake extends Applet implements KeyListener, ActionListener, Runnable {

    // ------------------------------
    // VARIABLES DEL JUEGO
    // ------------------------------

    int posX = 100, posY = 120;  // Posicion de la serpiente
    int comidaX, comidaY;        // Posicion de la comida

    // Tamaños
    int tamano = 20;             // Tamaño de la comida
    int anchoSerpiente = 35;     // Ancho de serpiente (rectangulo)
    int altoSerpiente = 20;      // Alto de serpiente

    // Contadores
    int puntaje = 0;
    int meta = 0;
    int tiempo = 0;

    // Direccion de movimiento
    // 0 arriba, 1 abajo, 2 izquierda, 3 derecha
    int direccion = 3;

    // Estados del juego
    boolean jugando = false;
    boolean gameOver = false;
    boolean victoria = false;

    // Velocidad ajustable segun nivel
    int velocidad = 100;

    // Botones de nivel
    Button btnFacil, btnMedio, btnDificil;

    // Hilo del juego
    Thread hilo;
    int contadorTiempo = 0;

    // Double buffering para evitar parpadeo
    Image buffer;
    Graphics bufferG;

    // ------------------------------
    // INICIALIZACION DEL APPLET
    // ------------------------------

    public void init() {

        setSize(600, 450); // Tamaño de la ventana del juego
        setLayout(null);
        addKeyListener(this);

        // -------- Botones --------
        btnFacil = new Button("Facil");
        btnFacil.setBounds(20, 10, 80, 30);
        add(btnFacil);
        btnFacil.addActionListener(this);

        btnMedio = new Button("Medio");
        btnMedio.setBounds(120, 10, 80, 30);
        add(btnMedio);
        btnMedio.addActionListener(this);

        btnDificil = new Button("Dificil");
        btnDificil.setBounds(220, 10, 80, 30);
        add(btnDificil);
        btnDificil.addActionListener(this);

        // Iniciar hilo
        hilo = new Thread(this);
        hilo.start();

        requestFocus(); // Necesario para captar teclas
    }

    // ------------------------------
    // INICIO DE NIVELES
    // ------------------------------

    public void iniciarNivel(String nivel) {
        jugando = true;
        gameOver = false;
        victoria = false;

        puntaje = 0;
        posX = 100;
        posY = 120;
        direccion = 3;

        // Configurar meta y tiempo segun nivel
        if (nivel.equals("facil")) {
            meta = 10;
            tiempo = 90;
            velocidad = 110; // Mas lento
        }
        else if (nivel.equals("medio")) {
            meta = 15;
            tiempo = 60;
            velocidad = 80; // Velocidad media
        }
        else if (nivel.equals("dificil")) {
            meta = 20;
            tiempo = 45;
            velocidad = 55; // Muy rapido
        }

        generarComida();
        repaint();
    }

    // ------------------------------
    // GENERAR COMIDA EN POSICION RANDOM
    // ------------------------------

    public void generarComida() {
        comidaX = (int)(Math.random() * ((getWidth() - tamano) / tamano)) * tamano;
        comidaY = 70 + (int)(Math.random() * ((getHeight() - 80) / tamano)) * tamano;
    }

    // ------------------------------
    // DOUBLE BUFFER – PARA ELIMINAR PARPADEO
    // ------------------------------

    public void update(Graphics g) {
        paint(g);
    }

    // ------------------------------
    // DIBUJAR JUEGO
    // ------------------------------

    public void paint(Graphics g) {

    // Crear buffer si no existe
    if (buffer == null) {
        buffer = createImage(getWidth(), getHeight());
        bufferG = buffer.getGraphics();
    }

        // ---------------------------
        // LIMPIAR EL HUD (PARTE BLANCA)
        // ---------------------------
        bufferG.setColor(Color.white);
        bufferG.fillRect(0, 0, getWidth(), 60);

        // Fondo amarillo del área del juego
        bufferG.setColor(Color.yellow);
        bufferG.fillRect(0, 60, getWidth(), getHeight() - 60);

        // -------------------
        // Serpiente con degradado
        // -------------------

        Graphics2D g2 = (Graphics2D) bufferG;

        GradientPaint grad = new GradientPaint(
            posX, posY, new Color(0, 255, 120),
            posX + anchoSerpiente, posY + altoSerpiente, new Color(0, 120, 255)
        );

        g2.setPaint(grad);
        g2.fillRoundRect(posX, posY, anchoSerpiente, altoSerpiente, 10, 10);

        // -------------------
        // Comida (circulo rojo)
        // -------------------
        bufferG.setColor(Color.red);
        bufferG.fillOval(comidaX, comidaY, tamano, tamano);

        // -------------------
        // HUD (Puntaje y tiempo)
        // -------------------

        bufferG.setColor(Color.black);
        bufferG.setFont(new Font("Arial", Font.BOLD, 16));
        bufferG.drawString("Puntaje: " + puntaje + " / " + meta, 350, 30);
        bufferG.drawString("Tiempo: " + tiempo, 350, 55);

        // -------------------
        // Mensaje de victoria
        // -------------------
        if (victoria) {
            bufferG.setFont(new Font("Arial", Font.BOLD, 26));
            bufferG.setColor(Color.blue);
            bufferG.drawString("GANASTE", 230, 230);
        }

        // -------------------
        // Mensaje de derrota
        // -------------------
        if (gameOver) {
            bufferG.setFont(new Font("Arial", Font.BOLD, 26));
            bufferG.setColor(Color.red);
            bufferG.drawString("GAME OVER", 220, 230);
        }

        // Pintar buffer en pantalla
        g.drawImage(buffer, 0, 0, null);
    }

    // ------------------------------
    // MOVIMIENTO DE LA SERPIENTE
    // ------------------------------

    public void mover() {
        if (!jugando) return;

        if (direccion == 0) posY -= tamano;
        if (direccion == 1) posY += tamano;
        if (direccion == 2) posX -= tamano;
        if (direccion == 3) posX += tamano;

        // Teletransportacion por bordes
        if (posX < 0) posX = getWidth();
        if (posX > getWidth()) posX = 0;
        if (posY < 60) posY = getHeight();
        if (posY > getHeight()) posY = 60;
    }

    // ------------------------------
    // DETECTAR COLISION CON COMIDA
    // ------------------------------

    public void verificarComida() {

        boolean colision = 
            posX < comidaX + tamano &&
            posX + anchoSerpiente > comidaX &&
            posY < comidaY + tamano &&
            posY + altoSerpiente > comidaY;

        if (colision) {
            puntaje++;
            generarComida();

            if (puntaje >= meta) {
                jugando = false;
                victoria = true;
            }
        }
    }

    // ------------------------------
    // BUCLE DEL JUEGO
    // ------------------------------

    public void run() {
        while (true) {
            if (jugando) {

                mover();
                verificarComida();
                repaint();

                try { Thread.sleep(velocidad); } catch(Exception e) {}

                // Contador de tiempo
                contadorTiempo++;
                if (contadorTiempo >= 10) {
                    tiempo--;
                    contadorTiempo = 0;
                }

                // Tiempo agotado
                if (tiempo <= 0) {
                    jugando = false;
                    gameOver = true;
                }
            }

            repaint();
        }
    }

    // ------------------------------
    // BOTONES (NIVELES)
    // ------------------------------

    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == btnFacil) iniciarNivel("facil");
        if (e.getSource() == btnMedio) iniciarNivel("medio");
        if (e.getSource() == btnDificil) iniciarNivel("dificil");

        requestFocus();
    }

    // ------------------------------
    // TECLAS (FLECHAS Y WASD)
    // ------------------------------

    public void keyPressed(KeyEvent e) {
        int tecla = e.getKeyCode();

        if (tecla == KeyEvent.VK_UP || tecla == KeyEvent.VK_W) direccion = 0;
        if (tecla == KeyEvent.VK_DOWN || tecla == KeyEvent.VK_S) direccion = 1;
        if (tecla == KeyEvent.VK_LEFT || tecla == KeyEvent.VK_A) direccion = 2;
        if (tecla == KeyEvent.VK_RIGHT || tecla == KeyEvent.VK_D) direccion = 3;
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
}
