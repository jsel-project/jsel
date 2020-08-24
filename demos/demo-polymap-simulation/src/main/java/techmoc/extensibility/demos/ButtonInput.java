package techmoc.extensibility.demos;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ButtonInput implements ActionListener {
  private ArrayList<ButtonListener> listeners = new ArrayList<>();
  public static final String NORTH_CLICKED = "North";
  public static final String SOUTH_CLICKED = "South";
  public static final String EAST_CLICKED = "East";
  public static final String WEST_CLICKED = "West";
  private String buttonClicked;


  /**
   * Invoked when an action occurs.
   *
   * @param e the event to be processed
   */
  @Override
  public void actionPerformed(ActionEvent e) {

    buttonClicked = e.getActionCommand();
    updateListeners();
  }

  private void updateListeners(){
    for (ButtonListener listener : listeners){
        listener.buttonClicked(buttonClicked);
    }
  }

  public void addListener(ButtonListener listener){
    listeners.add(listener);
  }

  public void removeListener(ButtonListener listener){
    listeners.remove(listener);
  }
}
