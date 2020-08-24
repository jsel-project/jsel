# Polymorphic Map Usage Demonstration
## Purpose
This sample program is an example of how a developer might use the polymorphic map to create mixed-type collections. The motivation for using the polymorphic map in this case, instead of other Java built-in types (such as the HashMap) is the individual pieces of information do not logically extend from a common subtype. While some of these elements could be encapsulated in a container type, that approach would create a much more complicated implementation since the need for some fields changes depending on the context.

## Scenario
This demonstration simulates a radio detector test bed application. The initial design requirement was to create an application to control a radio-frequency (RF) signal source, the *Emitter*, and 4 optional RF detectors located at equidistant locations from the Emitter along the cardinal directions. Initially, the design requirements only included two known detectors, the *Super Detector* and the *Mega Detector*. The designers were able to create an abstract base class due to the similar characteristics of those two detectors.
As is often the case, a new detector was invented that includes radically different capabilities. The *HawkEye-1000* is a newer, advanced RF detector that can also detect infrared (IR) emissions. Due to the unique way it scans for RF and IR emissions, it doesn't share any type ancestors with the Super or Mega detectors. This new detector changes the world because it can not only detect when an RF station is broadcasting - it can also "see" the emitter station's heat signature.
### Detector Characteristics
#### Super RF Detector
The Super RF Detector has a small, pencil beam detection area. Due to it's antique design, it can also only detect emissions within a small frequency range. The Super RF Detector will sometimes fail to detect the emitter because of these limiting parameters. 
#### Mega RF Detector
The Mega RF Detector is the next upgrade of Super. It has a much wider detection fan. It also has upgraded internals that expand the detection frequencies well beyond what the emitter can output. If the emitter is within the detection fan, the Mega will detect it.
#### HawkEye-1000
The HawkEye-1000 is a completely new type of sensor. It combines a super high sensitivity RF detector unit with an power bank of infrared detectors. To save power, the HawkEye will only register detections if it "sees" a target in IR. Once the emitter warms up a little, the HawkEye's super sensitive RF detector will not only detect emissions when the emitter is pointed directly at it, but it will also detect how hot the emitter is getting. The combination of sensors also lets the HawkEye sense the emitter, even when not being directly targeted.
### Emitter Characteristics
The Emitter is piece of equipment that is being tested to see how well other sensors can detect it. It broadcasts a narrow beam of high-power RF energy as it sweeps around in a circle. The broadcast frequency and power level fluctuate at a regular period (de-synchronized from rotational speed) so that detectors can be tested against any possible operating mode.  
## Applicability
The polymorphic map satisfies this use-case by allowing the designer to create several key abstractions without a lot of burdensome overhead.  
First there is the mechanism used to interact with the separate detector models.
```java
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
``` 
A `PolymorphicMap` is used to contain the detector at each cardinal location. This map contains only one key and one type. The key allows the application to determine the capabilities of the detector that is stored - it is either an "RF" or an "RF/IR" detector. If the position is empty, then no key is stored and the application can simply mark the location "Offline". If a key was stored, then the application can retrieve the type instance and call the appropriate methods to trigger the process model.
```java
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
```  

Next there is the message passing interface used to pass the emitter state parameters to each process model. This is another area where the `PolymorphicMap`'s low overhead is also incredibly useful. 
```java
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
```
Each detector is passed an `Emission` packet containing all of the emitter's state information. Instead of managing these and other messages, especially those with ephemeral fields, using the traditional factory patterns - the design simply uses a `PolymorphicMap`. Fields are added as needed, while still adhering to an interface specification. This allows each detector model to check for specific keys and find the information used for their processing. As an example of the flexibility offered by the `PolymorphicMap`, the Mega Detector also checks for a field that does not exist in the simulation.
```java
   if (emission.containsKey(RADAR_BAND)) {
      radarBand = emission.get(RADAR_BAND, String.class);
    }
```  
Finally, the detectors respond to the application with a `Detection` message that includes all of the parameters that were detected and their values. 
```java
  /* Example from MegaRFDetector */
  public PolymorphicMap getDetections() {
    PolymorphicMap detections = new PolymorphicMap();
    detections.put("Detection", detected);

    if (detected) {
      detections.put(FREQUENCY, frequency);
      detections.put(FREQUENCY_UNITS, frequencyUnits);
      detections.put(POWER, power);
      detections.put(POWER_UNITS, powerUnits);
      detections.put(SIGNAL_TYPE, signalType);
      detections.put(RADAR_BAND, radarBand);
    }
    return detections;
  }
```
This allows the application to tailor the displayed information on a per-detector basis so that even behavioral differences can be preserved. In this application demo, the HawkEye-1000 RF-IF detector must display not only the frequency and power of the RF signal, but also a measure of the IR energy it is detecting as the emitter heats up. 
