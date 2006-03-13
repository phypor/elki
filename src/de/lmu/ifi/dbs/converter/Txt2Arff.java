package de.lmu.ifi.dbs.converter;


import de.lmu.ifi.dbs.parser.AbstractParser;
import de.lmu.ifi.dbs.utilities.UnableToComplyException;
import de.lmu.ifi.dbs.utilities.Util;
import de.lmu.ifi.dbs.utilities.optionhandling.NoParameterValueException;
import de.lmu.ifi.dbs.utilities.optionhandling.UnusedParameterException;
import de.lmu.ifi.dbs.utilities.optionhandling.WrongParameterValueException;
import de.lmu.ifi.dbs.wrapper.StandAloneWrapper;

import java.io.*;
import java.text.ParseException;
import java.util.*;

/**
 * Converts a txt file to an arff file. All attributes that can be parsed as
 * doubles will be declared as numeric attributes, the others will be declared
 * as nominal attributes. The values for a nominal attribute are sorted.
 */
public class Txt2Arff extends StandAloneWrapper {
  static {
    INPUT_D = "<filename>the txt-file to convert";
    OUTPUT_D = "<filename>the arff-file to write the converted txt-file in";
  }

  /**
   * Main method to run this wrapper.
   *
   * @param args the arguments to run this wrapper
   */
  public static void main(String[] args) {
    Txt2Arff wrapper = new Txt2Arff();
    try {
      wrapper.run(args);
    }
    catch (WrongParameterValueException e) {
      System.err.println(wrapper.optionHandler.usage(e.getMessage()));
    }
    catch (NoParameterValueException e) {
      System.err.println(wrapper.optionHandler.usage(e.getMessage()));
    }
    catch (UnusedParameterException e) {
      System.err.println(wrapper.optionHandler.usage(e.getMessage()));
    }
    catch (UnableToComplyException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Runs the wrapper with the specified arguments.
   *
   * @param args parameter list
   */
  public void run(String[] args) throws UnableToComplyException {
    optionHandler.grabOptions(args);

    try {
      File inputFile = new File(getInput());

      List<WekaAttribute[]> attributeLines = new ArrayList<WekaAttribute[]>();
      BitSet nominal = new BitSet();
      WekaAttributeFactory wekaAttributeFactory = new WekaAttributeFactory();
      int dimensions = -1;
      BufferedReader br = new BufferedReader(new FileReader(inputFile));
      int lineNumber = 0;
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        lineNumber++;
        if (!line.startsWith(AbstractParser.COMMENT)) {
          String[] attributeStrings = AbstractParser.WHITESPACE_PATTERN.split(line);
          WekaAttribute[] attributes = new WekaAttribute[attributeStrings.length];
          if (dimensions == -1) {
            dimensions = attributes.length;
          }
          else if (dimensions != attributes.length) {
            throw new ParseException("irregular number of attributes in line " + lineNumber + " - expected number: " + dimensions + " found number: " + attributeStrings.length, lineNumber);
          }

          for (int d = 0; d < attributeStrings.length; d++) {
            attributes[d] = wekaAttributeFactory.getAttribute(attributeStrings[d]);
            nominal.set(d, attributes[d].isNominal());
          }
          attributeLines.add(attributes);
        }
      }
      br.close();
      Map<Integer, List<WekaAttribute>> nominalValues = new HashMap<Integer, List<WekaAttribute>>(nominal.cardinality(), 1);
      for (int i = nominal.nextSetBit(0); i >= 0; i = nominal.nextSetBit(i + 1)) {
        Set<WekaAttribute> nominalSet = new HashSet<WekaAttribute>();
        for (WekaAttribute[] attributeLine : attributeLines) {
          nominalSet.add(attributeLine[i]);
        }
        List<WekaAttribute> nominalList = new ArrayList<WekaAttribute>(nominalSet);
        Collections.sort(nominalList);
        nominalValues.put(i, nominalList);
      }

      File outputFile = new File(getOutput());
      if (outputFile.exists()) {
        outputFile.delete();
        if (isVerbose()) {
          System.out.println("The file " + getOutput() + " exists and was be replaced.");
        }
      }

      outputFile.createNewFile();
      PrintStream outStream = new PrintStream(new FileOutputStream(outputFile));

      outStream.print("@relation ");
      outStream.println(inputFile.getName());
      outStream.println();

      for (int i = 0; i < dimensions; i++) {
        outStream.print("@attribute d");
        outStream.print(i);
        outStream.print(" ");
        if (nominal.get(i)) {
          outStream.print("{");
          Util.print(nominalValues.get(i), ",", outStream);
          outStream.print("}");
        }
        else {
          outStream.print(WekaAttribute.NUMERIC);
        }
        outStream.println();
      }

      outStream.println();
      outStream.println("@data");
      for (WekaAttribute[] attributes : attributeLines) {
        Util.print(Arrays.asList(attributes), ",", outStream);
        outStream.println();
      }
      outStream.close();

    }
    catch (IOException e) {
      UnableToComplyException ue = new UnableToComplyException("I/O Exception occured. " + e.getMessage(), e);
      ue.fillInStackTrace();
      throw ue;
    }
    catch (ParseException e) {
      UnableToComplyException ue = new UnableToComplyException(e.getMessage(), e);
      ue.fillInStackTrace();
      throw ue;
    }

  }
}
