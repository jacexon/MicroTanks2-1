import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Klasa ta odpowiada za panel rozgrywki, umieszczony on jest w klasie <code>GameFrame</code>.
 *
 */
public class GamePanel extends JPanel implements KeyListener {
    private Font font1=new Font("Calibri",Font.BOLD,20), font2=new Font("Calibri",Font.BOLD,12);
    private Tank[] tank;
    private Tank currentTank;
    Thread[] tankThreads;
    private Player player1 = new Player(NewGame.getColor1(),NewGame.getName1());
    private Player player2 = new Player(NewGame.getColor2(),NewGame.getName2());

    private int width,height;
    private int numberOfTanks=NewGame.getNumOfTanks();
    private int turnNumber=1;
    private long startTime;
    public Color firstColor=NewGame.getRealColor1(), secondColor=NewGame.getRealColor2();

    private int direction;
    private boolean collisionDetected=false;
    /**
     * Konstruktor klasy <code>GamePanel</code> przyjmuje wartości szerokości i wysokości panelu rozgrywki.
     * @param x Szerokość panelu gry
     * @param y Wysokość panelu gry
     */

    public GamePanel(int x,int y) {
        width=x;
        height=y;
        setPreferredSize(new Dimension(width, height));
        tank = new Tank[numberOfTanks];
        tankThreads = new Thread[numberOfTanks];
        createTanks(tank);
        setDoubleBuffered(true);
        setFocusable(true);

    }

    public Tank[] createTanks(Tank[] tank){
        for (int i=0;i<numberOfTanks;i++) {
            tankThreads[i]=new Thread(tank[i]);
            tank[i] = new Tank(width, 200, tankThreads[i]);
            if(i%2==0)
                tank[i].colorOfTank=firstColor;
            else
                tank[i].colorOfTank=secondColor;
        }

        selectATank(tank[0]);
        return tank;
    }

    public void selectATank(Tank tank){

        currentTank=tank;
        //addKeyListener(this);
        System.out.println("teraz czolg"+currentTank.getX());
        //currentTank.addNotify();
        currentTank.setReadyToShot(true);
        repaint();
    }
    public void moveOfTank(int dir) throws InterruptedException {
        currentTank.setCurrentTankStartPosition(currentTank.getX());
        currentTank.setEndOfMove(false);
        direction=dir;
    }

    public void shot(){
        currentTank.setReadyToShot(true);
        currentTank.releaseTheBullet();
        repaint();
    }

    public void setAngle(int angle){
        currentTank.setAngleOfShot(angle);
    }

    public void setStregth(int strength){
        currentTank.setStrengthOfShot(strength);
    }

    public void nextTurn(){
        System.out.println("zmiana"+turnNumber + "    "+numberOfTanks);
        if(turnNumber<numberOfTanks) {
            turnNumber+=1;

        }
        else
            turnNumber=1;

        System.out.println("zmiana"+turnNumber);
        selectATank(tank[turnNumber-1]);
    }

    /**
     * Metoda rysująca planszę w zależności od podanej jej funkcji matematycznej,
     * a także czołgi.
     *
     * @param g Kontekst graficzny
     * @return Brak
     */
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        double x,x2,y,y2;
        int[] formax = new int[2];
        int[] collisioCoordinates = new int[2];
        loadMap(formax);


        int[] groundCoordinates = new int[this.getWidth()+2];
        groundCoordinates=countGroundCoordinates(formax);
        Polygon ground = new Polygon();

        ground.addPoint(0,this.getHeight());
        for (int i = 1; i<this.getWidth()+1;i++){
            ground.addPoint(i,groundCoordinates[i]);
        }
        ground.addPoint(this.getWidth()-1,this.getHeight());
        ground.addPoint(0,this.getHeight());

        for(int i=0;i<this.getWidth();i++)
        {
            for(int j=0;j<numberOfTanks;j++) {
                tank[j].setyCoordinates(groundCoordinates[i+1], i);
            }
            // line[i]=new Line2D.Double(x,y,x2,y2);
        }
        // normowanie punktów
        float xScale = (float) getWidth()/640;
        float yScale = (float) getHeight()/380;
        // skalowanie komponentów
        ((Graphics2D) g).scale(xScale, yScale);
        g.setColor(Color.green);
        g.fillPolygon(ground);
        g.drawPolygon(ground);
        // System.out.println(currentTank.getX()+"current Tank");
        try {
            currentTank.move(direction);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(int j=0;j<numberOfTanks;j++) {
            tank[j].setRandomY();
            tank[j].draw(g);

        }

        collisioCoordinates=detectCollision(ground);

        if(collisionDetected==true){

            System.out.println("trafienie");
            collisionDetected=false;
        }

        Collisons collison = new Collisons(getWidth(),getHeight(),g,collisioCoordinates[0],collisioCoordinates[1]);




        if(turnNumber%2==1) {
            g.setFont(font1);
            g.setColor(firstColor);
            g.drawString(player1.getName(), getWidth() / 20, getHeight() / 20);
            g.drawString(Integer.toString(player1.getPoints()), getWidth() / 20, getHeight() / 20 + 20);
            g.setFont(font2);
            g.setColor(secondColor);
            g.drawString(player2.getName(), 5 * getWidth() / 8, getHeight() / 20);
            g.drawString(Integer.toString(player2.getPoints()), 5 * getWidth() / 8, getHeight() / 20 + 20);
        }
        else {
            g.setFont(font2);
            g.setColor(firstColor);
            g.drawString(player1.getName(), getWidth() / 20, getHeight() / 20);
            g.drawString(Integer.toString(player1.getPoints()), getWidth() / 20, getHeight() / 20 + 20);
            g.setFont(font1);
            g.setColor(secondColor);
            g.drawString(player2.getName(), 5 * getWidth() / 8, getHeight() / 20);
            g.drawString(Integer.toString(player2.getPoints()), 5 * getWidth() / 8, getHeight() / 20 + 20);
        }



        repaint();
    }

    public int[] countGroundCoordinates(int[] coefficient){
        int[] groundCoordinates = new int[this.getWidth()+2];

        for (int i = 1; i<this.getWidth()+2;i++) {
            groundCoordinates[i] =(int) (200 + coefficient[0] * Math.sin((double) (i) / 50) + coefficient[1]);
        }
        return groundCoordinates;
    }

    private int[] detectCollision(Polygon ground){
        int numberOfShootingTank=0;
        int[] coordinatesOfCollison=new int[2];

        for(int i=0;i<numberOfTanks;i++){
            if(tank[i].isShooting())
                numberOfShootingTank=i;
        }

        for(int i=0;i<numberOfTanks;i++){
            if(tank[numberOfShootingTank].getBulletFigure().intersects(tank[i].getTankFigure())) {
                System.out.println("czolg trafiony");
                //collisionDetected = true;
            }

            if(ground.intersects(tank[numberOfShootingTank].getBulletFigure()))
                System.out.println("ziemia trafiona");

            if((tank[numberOfShootingTank].getBulletFigure().intersects(tank[i].getTankFigure())) || (ground.intersects(tank[numberOfShootingTank].getBulletFigure()))) {
                collisionDetected = true;
                coordinatesOfCollison[0]=tank[numberOfShootingTank].getXBullet();
                coordinatesOfCollison[1]=tank[numberOfShootingTank].getyBullet();
            }
        }

        return coordinatesOfCollison;
    }


    public void setTanksCoordinates(int x)
    {

    }
    /**
     * Metoda parsująca informacje o planszy do utworzenia z plików
     * konfiguracyjnych.
     * @return Współczynniki funkcji
     * @throws IOException
     */


    private int[] loadMap(int[] form)
    {
        String a = "1",b = "2";
        try {
            File mapFile = new File("maplvl2.properties");
            Properties pro = new Properties();
            FileInputStream fis = new FileInputStream(mapFile);
            pro.load(fis);
            a = pro.getProperty("a");
            b = pro.getProperty("b");
        } catch (IOException e) {
            e.printStackTrace();
        }
        form[0] = Integer.parseInt(a);
        form[1] = Integer.parseInt(b);

        return form;
    }



    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Metoda ustawiająca kierunek czołgu.
     * @param e KeyEvent
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == e.VK_LEFT) {
            currentTank.setXDirection(-1);
            // System.out.println("dziala");
        }
        if (keyCode == e.VK_RIGHT) {
            currentTank.setXDirection(1);
        }
        if(keyCode == e.VK_SPACE){
            //currentTank.startTime=System.currentTimeMillis();
            currentTank.releaseTheBullet();
        }
    }

    /**
     * Metoda zatrzymująca czołg w momencie puszczenia klawisza kierunkowego(srzałki).
     *
     * @param e KeyEvent
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == e.VK_LEFT) {
            currentTank.setXDirection(0);
        }
        if (keyCode == e.VK_RIGHT) {
            currentTank.setXDirection(0);
        }
    }

}