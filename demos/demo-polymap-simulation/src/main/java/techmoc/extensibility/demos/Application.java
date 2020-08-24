package techmoc.extensibility.demos;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;

public class Application {

  private static final int WIDTH = 1024;
  private static final int HEIGHT = 768;
  private Canvas mapCanvas = new Canvas();
  private JPanel panel = new JPanel();
  private boolean running = false;
  private ArrayList<Updatable> updatables = new ArrayList<>();
  private ArrayList<Renderable> renderables = new ArrayList<>();
  private ButtonInput input = new ButtonInput();
  private static DisplayArea displayArea = new DisplayArea();
  public static final int FRAMES_PER_SECOND = 60;
  public static int getWidth(){
    return WIDTH;
  }

  public static int getHeight(){
    return HEIGHT-200;
  }

  public static void main(String[] args) {
    Application app = new Application();
    app.addRenderable(displayArea);
    app.addUpdatable(displayArea);
    app.start();
  }


  public void start(){

    Dimension windowSize = new Dimension(Application.WIDTH, Application.HEIGHT);
    JFrame mainWindow = new JFrame("Detector Tester");
    JPanel buttonPanel = new JPanel();
    JPanel mapPanel = new JPanel();

    buttonPanel.setLayout(new GridBagLayout());
    GridBagConstraints constraints = new GridBagConstraints();

    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    mainWindow.setSize(windowSize);
    mainWindow.setResizable(false);
    mainWindow.setLayout(new FlowLayout());

    mapCanvas.setBounds(0, 0,Application.getWidth(),Application.getHeight());
    JButton northDetector = new JButton("North Detector");
    JButton southDetector = new JButton("South Detector");
    JButton eastDetector = new JButton("East Detector");
    JButton westDetector = new JButton("West Detector");

    northDetector.setActionCommand(ButtonInput.NORTH_CLICKED);
    southDetector.setActionCommand(ButtonInput.SOUTH_CLICKED);
    eastDetector.setActionCommand(ButtonInput.EAST_CLICKED);
    westDetector.setActionCommand(ButtonInput.WEST_CLICKED);

    final int buttonHeight = 30;
    final int buttonWidth = 160;
    northDetector.setBounds(2*buttonWidth, Application.getHeight() + buttonHeight, buttonWidth, buttonHeight);
    southDetector.setBounds(2*buttonWidth, Application.getHeight() + (3*buttonHeight), buttonWidth, buttonHeight);
    eastDetector.setBounds(buttonWidth, Application.getHeight() + (2*buttonHeight), buttonWidth, buttonHeight);
    westDetector.setBounds(3*buttonWidth, Application.getHeight() + (2*buttonHeight), buttonWidth, buttonHeight);
    buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    int constraintsFill = GridBagConstraints.HORIZONTAL;
    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.fill = constraintsFill;
    buttonPanel.add(northDetector, constraints);
    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.fill = constraintsFill;
    buttonPanel.add(southDetector, constraints);
    constraints.gridx = 2;
    constraints.gridy = 2;
    constraints.fill = constraintsFill;
    buttonPanel.add(eastDetector, constraints);
    constraints.gridx = 0;
    constraints.gridy = 2;
    constraints.fill = constraintsFill;
    buttonPanel.add(westDetector, constraints);
    JLabel label = new JLabel("Detector Positions");
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.fill = constraintsFill;
    buttonPanel.add(label, constraints);

    constraints.gridx = 3;
    constraints.gridy = 1;
    constraints.fill = constraintsFill;
    mapPanel.add(mapCanvas, constraints);


    mainWindow.add(mapPanel);
    mainWindow.add(buttonPanel);
    mainWindow.setVisible(true);
    mainWindow.setLocationRelativeTo(null);


    final int TIME_PER_TICK = 1000/FRAMES_PER_SECOND;
    final int MAX_FRAME_SKIPS = 5;

    long nextTick = System.currentTimeMillis();
    int loops;
    float interpolation;
    int ticks = 0;

    input.addListener(displayArea);
    northDetector.addActionListener(input);
    southDetector.addActionListener(input);
    eastDetector.addActionListener(input);
    westDetector.addActionListener(input);

    /* Start the real-time loop */
    running = true;
    while(running){

      /* Update */
      loops = 0;
      while(System.currentTimeMillis() > nextTick && loops < MAX_FRAME_SKIPS){
        update();
        ticks++;
        nextTick += TIME_PER_TICK;
        loops++;
      }

      /* Render */
      interpolation = (float) (System.currentTimeMillis() + TIME_PER_TICK - nextTick)/(float) TIME_PER_TICK;
      render(interpolation);
    }
  }

  private void update(){
    for (Updatable updatable : updatables){
        updatable.update();
    }
  }

  private void render(float interpolation){
    BufferStrategy bufferStrategy = mapCanvas.getBufferStrategy();
    if(bufferStrategy == null){
      final int DOUBLE_BUFFER = 2;
      mapCanvas.createBufferStrategy(DOUBLE_BUFFER);
      return;
    }
    Graphics2D graphics2D = (Graphics2D) bufferStrategy.getDrawGraphics();
    graphics2D.clearRect(0,0, Application.WIDTH, Application.HEIGHT);
    for (Renderable renderable : renderables){
        renderable.render(graphics2D, interpolation);
    }
    graphics2D.dispose();
    bufferStrategy.show();
  }

  public void addUpdatable(Updatable updatable){
    updatables.add(updatable);
  }

  public void addRenderable(Renderable renderable){
    renderables.add(renderable);
  }

  public void removeUpdatable(Updatable updatable){
    updatables.remove(updatable);
  }

  public void removeRenderable(Renderable renderable){
    renderables.remove(renderable);
  }
}
