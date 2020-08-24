package techmoc.extensibility.demos;

import techmoc.extensibility.polymorphicmap.PolymorphicMap;

import java.awt.*;

import static techmoc.extensibility.demos.DemoColors.*;
import static techmoc.extensibility.demos.DetectionConstants.*;
import static techmoc.extensibility.demos.HawkeyeDetector.*;

/**
 * Draws the upper canvas portion of the application with a division between the "test bench" area and a "status area".
 */
public class DisplayArea implements Updatable, Renderable, ButtonListener {

  private final String NORTH = "North";
  private final String SOUTH = "South";
  private final String EAST = "East";
  private final String WEST = "West";

  private final int iconSide = 25;
  private final Font labelFont = new Font("default", Font.BOLD, 14);
  private final Font statusFont = new Font("default", Font.BOLD, 22);

  /* Fix the positions of the four detectors and the central emitter */
  private final int northX = (int) (.25 * Application.getWidth());
  private final int northY = (int) (.25 * Application.getHeight());
  private final int southX = (int) (.25 * Application.getWidth());
  private final int southY = (int) (.75 * Application.getHeight());
  private final int eastX = (int) (.375 * Application.getWidth());
  private final int eastY = (int) (.50 * Application.getHeight());
  private final int westX = (int) (.125 * Application.getWidth());
  private final int westY = (int) (.50 * Application.getHeight());
  private final int centerX = (int) (.25 * Application.getWidth());
  private final int centerY = (int) (.50 * Application.getHeight());

  private final String OFFLINE = "Offline";
  String northDetectorLabel = "North Detector";
  String southDetectorLabel = "South Detector";
  String eastDetectorLabel = "East Detector";
  String westDetectorLabel = "West Detector";
  String northDetectionMessage = "";
  String southDetectionMessage = "";
  String eastDetectionMessage = "";
  String westDetectionMessage = "";
  private boolean northActive = false;
  private boolean southActive = false;
  private boolean eastActive = false;
  private boolean westActive = false;
  private int currentNorth = 0;
  private int currentSouth = 0;
  private int currentEast = 0;
  private int currentWest = 0;
  private int numStates = 4;
  private PolymorphicMap detectors = new PolymorphicMap();
  private Emitter emitter = new Emitter();

  /**
   * Draw all Renderables to the display as necessary.
   *
   * @param graphics      Reference to the drawable surface
   * @param interpolation interpolated sub-frame step
   */
  @Override
  public void render(Graphics2D graphics, float interpolation) {

    /* Draw the "Test Bench" Area */
    colorDisplayAreas(graphics);
    drawEmitter(graphics, interpolation);
    renderDetectors(graphics, interpolation);

    /* Draw the "Status" Area */
    drawStatuses(graphics);

  }

  /**
   * Color the left and right (test and status) areas of the
   *
   * @param graphics Reference to the graphics object for the display area
   */
  private void colorDisplayAreas(Graphics2D graphics) {
    graphics.setColor(translucentGray);
    graphics.setColor(underlayGray);
    graphics.fillRect(0, 0, (int) (.5 * Application.getWidth()), Application.getHeight());
    graphics.setColor(translucentGray);
    graphics.fillRect((int) (.5 * Application.getWidth()), 0, (int) (.5 * Application.getWidth()), Application.getHeight());
    graphics.setColor(translucentGreen);
    graphics.fillRect((int) (.5 * Application.getWidth()), 0, (int) (.5 * Application.getWidth()), Application.getHeight());
  }

  /**
   * Draws the emitter icon and scan beam to the display area.
   *
   * @param graphics      Reference to the graphics object for the display area
   * @param interpolation interpolated sub-frame step
   */
  private void drawEmitter(Graphics2D graphics, float interpolation) {

    graphics.setColor(graphite);
    graphics.setFont(labelFont);
    graphics.drawString("Emitter", centerX, centerY + iconSide + 15);
    graphics.setColor(graphite);
    emitter.setPosition(centerX, centerY);
    emitter.render(graphics, interpolation);

  }

  /**
   * Draws a detector icon at a specified position on the display area.
   *
   * @param positionName   Cardinal position name
   * @param positionX     x-coordinate
   * @param positionY     y-coordinate
   * @param graphics      Reference to the graphics object for the display area
   * @param interpolation interpolated sub-frame step
   */
  private void drawDetector(String positionName, int positionX, int positionY, Graphics2D graphics, float interpolation) {
    if (detectors.containsKeyOfType(positionName, RFDetector.class)) {
      detectors.get(positionName, RFDetector.class).setPosition(positionX, positionY);
      detectors.get(positionName, RFDetector.class).render(graphics, interpolation);
    }
    if (detectors.containsKeyOfType(positionName, HawkeyeDetector.class)) {
      detectors.get(positionName, HawkeyeDetector.class).setLocation(positionX, positionY);
      detectors.get(positionName, HawkeyeDetector.class).render(graphics, interpolation);
    }
  }

  /**
   * Draws a detector name label at a specified position on the display area.
   *
   * @param label     Text to be drawn on the display
   * @param positionX x-coordinate
   * @param positionY y-coordinate
   * @param graphics  Reference to the graphics object for the display area
   */
  private void drawDetectorLabel(String label, int positionX, int positionY, Graphics2D graphics) {
    graphics.setColor(graphite);
    graphics.setFont(labelFont);
    graphics.drawString(label, positionX, positionY);
  }

  /**
   * Call the render method for any of the active detectors.
   *
   * @param graphics      Reference to the graphics object for the display area
   * @param interpolation interpolated sub-frame step
   */
  private void renderDetectors(Graphics2D graphics, float interpolation) {
    if (northActive) {
      drawDetector(NORTH, northX, northY, graphics, interpolation);
      drawDetectorLabel(northDetectorLabel, northX, northY + iconSide + 15, graphics);
    }
    if (southActive) {
      drawDetector(SOUTH, southX, southY, graphics, interpolation);
      drawDetectorLabel(southDetectorLabel, southX, southY + iconSide + 15, graphics);
    }
    if (eastActive) {
      drawDetector(EAST, eastX, eastY, graphics, interpolation);
      drawDetectorLabel(eastDetectorLabel, eastX, eastY + iconSide + 15, graphics);
    }
    if (westActive) {
      drawDetector(WEST, westX, westY, graphics, interpolation);
      drawDetectorLabel(westDetectorLabel, westX, westY + iconSide + 15, graphics);
    }
  }

  /**
   * Draw the status message overlays in the status area.
   *
   * @param graphics Reference to the graphics object for the display area
   */
  private void drawStatuses(Graphics2D graphics) {
    int base = 40;
    int lineOffset = 40;
    int sectionOffset = 120;
    int positionX = (int) (.5 * Application.getWidth()) + 5;

    int lineY = base + lineOffset;
    int sectionY = base;
    drawPositionStatusHeader(NORTH, positionX, sectionY, graphics);
    drawDetectorStatusLine(NORTH, northDetectionMessage, positionX, lineY, northActive, graphics);

    lineY += sectionOffset;
    sectionY += sectionOffset;
    drawPositionStatusHeader(SOUTH, positionX, sectionY, graphics);
    drawDetectorStatusLine(SOUTH, southDetectionMessage, positionX, lineY, southActive, graphics);

    lineY += sectionOffset;
    sectionY += sectionOffset;
    drawPositionStatusHeader(EAST, positionX, sectionY, graphics);
    drawDetectorStatusLine(EAST, eastDetectionMessage, positionX, lineY, eastActive, graphics);

    lineY += sectionOffset;
    sectionY += sectionOffset;
    drawPositionStatusHeader(WEST, positionX, sectionY, graphics);
    drawDetectorStatusLine(WEST, westDetectionMessage, positionX, lineY, westActive, graphics);

    lineY += sectionOffset;
    sectionY += sectionOffset;
    drawPositionStatusHeader("Emitter", positionX, sectionY, graphics);
    drawEmitterStatusLine(positionX, lineY, graphics);
  }

  /**
   * Draws the "xx Status:" line to the status panel for each of the displayable positions.
   *
   * @param position  Name used for the display
   * @param positionX x-coordinate
   * @param positionY y-coordinate
   * @param graphics  Reference to the graphics object for the display area
   */
  private void drawPositionStatusHeader(String position, int positionX, int positionY, Graphics2D graphics) {
    graphics.setColor(graphite);
    graphics.setFont(statusFont);
    graphics.drawString(String.format("%s Status:", position), positionX, positionY);
  }

  private void drawDetectorStatusLine(String position, String message, int positionX, int positionY, boolean active, Graphics2D graphics) {

    if (active) {
      if (detectors.containsKeyOfType(position, RFDetector.class)) {
        if (detectors.get(position, RFDetector.class).detected()) {
          graphics.setColor(darkGreen);
          graphics.drawString(message, positionX, positionY);
        } else {
          graphics.setColor(darkRed);
          graphics.drawString("Not Detecting", positionX, positionY);
        }
      }
      if (detectors.containsKeyOfType(position, HawkeyeDetector.class)) {
        switch (detectors.get(position, HawkeyeDetector.class).getScanState()) {
          case RF_IR_ACQUIRED:
            graphics.setColor(darkGreen);
            graphics.drawString(message, positionX, positionY);
            break;
          case IR_ACQUIRED:
            graphics.setColor(darkYellow);
            graphics.drawString(message, positionX, positionY);
            break;
          case NO_ACQUISITION: /* Fall Through */
          default:
            graphics.setColor(darkRed);
            graphics.drawString("No Acquisition", positionX, positionY);
        }
      }
    } else {
      graphics.setColor(graphite);
      graphics.drawString(OFFLINE, positionX, positionY);
    }
  }

  private void drawEmitterStatusLine(int positionX, int positionY, Graphics2D graphics) {
    if (emitter.isCold()) {
      graphics.setColor(darkBlue);
    }
    if (emitter.isNormal()) {
      graphics.setColor(darkGreen);
    }
    if (emitter.isHot()) {
      graphics.setColor(darkRed);
    }
    Double temperature = emitter.getTransmitterTemperature();
    String status = emitter.getTransmitterStatus();
    graphics.drawString(String.format("Transmitting | Temp: %3.3f - %s", temperature, status), positionX, positionY);
  }

  /**
   * Process the results of an RFDetector detection.
   *
   * @param map <code>PolymorphicMap</code> containing the results of the detection.
   * @return <code>String</code> result of the detection
   */
  private String getDetectionMessage(PolymorphicMap map) {
    Double freq = map.get(FREQUENCY, Double.class);
    String freqUnits = map.get(FREQUENCY_UNITS, String.class);
    Double power = map.get(POWER, Double.class);
    String powerUnits = map.get(POWER_UNITS, String.class);
    String signalType = map.get(SIGNAL_TYPE, String.class);
    return String.format("%3s detected: %-4.3f %3s at %-4.3f %3s", signalType, freq, freqUnits, power, powerUnits);
  }

  /**
   * Process the results of a HawkEye-1000 scan.
   *
   * @param map <code>PolymorphicMap</code> containing the results of the scan.
   * @return <code>String</code> result of the scan
   */
  private String getScanMessage(PolymorphicMap map) {
    Double freq = map.get(FREQUENCY, Double.class);
    String freqUnits = map.get(FREQUENCY_UNITS, String.class);
    Double power = map.get(POWER, Double.class);
    String powerUnits = map.get(POWER_UNITS, String.class);
    String signalType = map.get(SIGNAL_TYPE, String.class);
    String temperature = map.get(TEMPERATURE, String.class);
    return String.format("%6s: %-4.3f %s/%-4.3f %s/%s", signalType, freq, freqUnits, power, powerUnits, temperature);
  }

  /**
   * Advance the simulation state by periodically calling the appropriate model routines for all simulated entities.
   */
  @Override
  public void update() {
    emitter.update();

    /* Pack the emission map for any available detectors */
    PolymorphicMap emission = new PolymorphicMap();
    emission.put(TEMPERATURE, emitter.getTransmitterTemperature());
    emission.put(STATUS, emitter.getTransmitterStatus());
    emission.put(FREQUENCY, emitter.getFrequencyMHz());
    emission.put(FREQUENCY_UNITS, "MHz");
    emission.put(POWER, emitter.getPowerMilliwatts());
    emission.put(POWER_UNITS, "mW");
    emission.put(EMITTER_X, emitter.getX());
    emission.put(EMITTER_Y, emitter.getY());
    emission.put(BEAM_ANGLE, emitter.getBeamAngle());
    emission.put(BEAM_WIDTH, emitter.getBeamWidth());

    if (northActive) {
      /* Check to see if either detector type is available */
      if (detectors.containsKeyOfType(NORTH, RFDetector.class)) {
        detectors.get(NORTH, RFDetector.class).detect(emission);
        detectors.get(NORTH, RFDetector.class).update();
        setDetectionMessage(NORTH, getDetectionMessage(detectors.get(NORTH, RFDetector.class).getDetections()));
      } else if (detectors.containsKeyOfType(NORTH, HawkeyeDetector.class)) {
        detectors.get(NORTH, HawkeyeDetector.class).scan(emission);
        detectors.get(NORTH, HawkeyeDetector.class).update();
        setDetectionMessage(NORTH, getScanMessage(detectors.get(NORTH, HawkeyeDetector.class).getScanResult()));
      }
    }
    if (southActive) {
      /* Check to see if either detector type is available */
      if (detectors.containsKeyOfType(SOUTH, RFDetector.class)) {
        detectors.get(SOUTH, RFDetector.class).detect(emission);
        detectors.get(SOUTH, RFDetector.class).update();
        setDetectionMessage(SOUTH, getDetectionMessage(detectors.get(SOUTH, RFDetector.class).getDetections()));
      } else if (detectors.containsKeyOfType(SOUTH, HawkeyeDetector.class)) {
        detectors.get(SOUTH, HawkeyeDetector.class).scan(emission);
        detectors.get(SOUTH, HawkeyeDetector.class).update();
        setDetectionMessage(SOUTH, getScanMessage(detectors.get(SOUTH, HawkeyeDetector.class).getScanResult()));
      }
    }
    if (eastActive) {
      /* Check to see if either detector type is available */
      if (detectors.containsKeyOfType(EAST, RFDetector.class)) {
        detectors.get(EAST, RFDetector.class).detect(emission);
        detectors.get(EAST, RFDetector.class).update();
        setDetectionMessage(EAST, getDetectionMessage(detectors.get(EAST, RFDetector.class).getDetections()));
      } else if (detectors.containsKeyOfType(EAST, HawkeyeDetector.class)) {
        detectors.get(EAST, HawkeyeDetector.class).scan(emission);
        detectors.get(EAST, HawkeyeDetector.class).update();
        setDetectionMessage(EAST, getScanMessage(detectors.get(EAST, HawkeyeDetector.class).getScanResult()));
      }
    }
    if (westActive) {
      /* Check to see if either detector type is available */
      if (detectors.containsKeyOfType(WEST, RFDetector.class)) {
        detectors.get(WEST, RFDetector.class).detect(emission);
        detectors.get(WEST, RFDetector.class).update();
        setDetectionMessage(WEST, getDetectionMessage(detectors.get(WEST, RFDetector.class).getDetections()));
      } else if (detectors.containsKeyOfType(WEST, HawkeyeDetector.class)) {
        detectors.get(WEST, HawkeyeDetector.class).scan(emission);
        detectors.get(WEST, HawkeyeDetector.class).update();
        setDetectionMessage(WEST, getScanMessage(detectors.get(WEST, HawkeyeDetector.class).getScanResult()));
      }
    }
  }

  /**
   * Creates a <code>PolymorphicMap</code> that contains maps an abstract detector type name to an instance of a specific
   * type of detector based on required functionality. This method allows for arbitrary detector taxonomies to be passed
   * to the update and rendering functions even if there are no common ancestor classes or interfaces.
   *
   * @param id Unique ID to represent what type of detector is required
   * @return <code>PolymorphicMap</code> containing a key->instance pair or an empty map if no such mapping can be decided
   */
  private PolymorphicMap getDetector(int id) {
    PolymorphicMap p = new PolymorphicMap();
    switch (id) {
      case 1:
        p.put(RFDETECTOR, new SuperRFDetector());
        break;
      case 2:
        p.put(RFDETECTOR, new MegaRFDetector());
        break;
      case 3:
        p.put(RFIRDETECTOR, new HawkeyeDetector());
        break;
      default:
        return null;
    }
    return p;
  }

  /**
   * Populates the field that reports the detector's common name on the display area's test panel (left-hand panel).
   *
   * @param detector Cardinal direction (button name) of the detector that this label pertains to
   * @param value    Name that should be displayed with the detector
   */
  private void setDetectorLabel(String detector, String value) {
    switch (detector) {
      case NORTH:
        northDetectorLabel = value;
        break;
      case SOUTH:
        southDetectorLabel = value;
        break;
      case EAST:
        eastDetectorLabel = value;
        break;
      case WEST:
        westDetectorLabel = value;
        break;
    }
  }

  /**
   * Populates the status message that will be displayed for the given detector in the status panel (right-hand panel).
   *
   * @param detector Cardinal direction (button name) of the detector that this message pertains to
   * @param value    Message that should be displayed
   */
  private void setDetectionMessage(String detector, String value) {
    switch (detector) {
      case NORTH:
        northDetectionMessage = value;
        break;
      case SOUTH:
        southDetectionMessage = value;
        break;
      case EAST:
        eastDetectionMessage = value;
        break;
      case WEST:
        westDetectionMessage = value;
        break;
    }
  }


  /**
   * Handles the detector select button click event.
   *
   * @param data Name of the button that was pressed.
   */
  @Override
  public void buttonClicked(String data) {
    switch (data) {
      case NORTH:
        currentNorth = selectNextDetector(NORTH, currentNorth);
        northActive = (currentNorth != 0);
        break;
      case SOUTH:
        currentSouth = selectNextDetector(SOUTH, currentSouth);
        southActive = (currentSouth != 0);
        break;
      case EAST:
        currentEast = selectNextDetector(EAST, currentEast);
        eastActive = (currentEast != 0);
        break;
      case WEST:
        currentWest = selectNextDetector(WEST, currentWest);
        westActive = (currentWest != 0);
        break;
    }
  }

  /**
   * Selects the next detector to display in a rotary fashion based on the current detector displayed.
   *
   * @param position     Cardinals position (button name) of the detector to advance
   * @param currentState Value of the detector being displayed
   * @return Updated state value
   */
  private int selectNextDetector(String position, int currentState) {
    currentState = (currentState + 1) % numStates;
    PolymorphicMap thisDetector = getDetector(currentState);
    if (currentState != 0) {
      if (thisDetector.containsKey(RFDETECTOR)) {
        detectors.put(position, thisDetector.get(RFDETECTOR, RFDetector.class));
        setDetectorLabel(position, detectors.get(position, RFDetector.class).getName());
      } else if (thisDetector.containsKey(RFIRDETECTOR)) {
        detectors.put(position, thisDetector.get(RFIRDETECTOR, HawkeyeDetector.class));
        setDetectorLabel(position, detectors.get(position, HawkeyeDetector.class).getIdent());
      }
    }
    return currentState;
  }

}
