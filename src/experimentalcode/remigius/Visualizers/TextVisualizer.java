package experimentalcode.remigius.Visualizers;

import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;

import de.lmu.ifi.dbs.elki.data.DoubleVector;
import de.lmu.ifi.dbs.elki.database.Database;
import de.lmu.ifi.dbs.elki.logging.LoggingUtil;
import de.lmu.ifi.dbs.elki.result.AnnotationResult;
import de.lmu.ifi.dbs.elki.visualization.css.CSSClass;
import de.lmu.ifi.dbs.elki.visualization.svg.SVGPlot;
import experimentalcode.remigius.ShapeLibrary;
import experimentalcode.remigius.VisualizationManager;
import experimentalcode.remigius.gui.ToolTipListener;

public class TextVisualizer<O extends DoubleVector> extends PlanarVisualizer<O> {

  private AnnotationResult<Double> anResult;
  private static final String NAME = "ToolTips";
  
  public TextVisualizer() {
  }

  public void setup(Database<O> database, AnnotationResult<Double> anResult, VisualizationManager<O> visManager) {

    init(database, visManager, NAME);
    this.anResult = anResult;
    setupCSS();
  }

  private void setupCSS() {

    CSSClass tooltip = visManager.createCSSClass(ShapeLibrary.TOOLTIP);
    tooltip.setStatement(SVGConstants.CSS_FONT_SIZE_PROPERTY, "0.1%");
    visManager.registerCSSClass(tooltip);
  }

  private Double getValue(int id) {

    return anResult.getValueFor(id);
  }

  @Override
  public Element visualize(SVGPlot svgp) {

    Element layer = ShapeLibrary.createSVG(svgp.getDocument());
    
    for (int id : database.getIDs()) {
      Element tooltip = ShapeLibrary.createToolTip(svgp.getDocument(),
          getPositioned(database.get(id), dimx), (1 - getPositioned(
              database.get(id), dimy)), getValue(id), id, dimx,
              dimy, toString());

      String dotID = ShapeLibrary.createID(ShapeLibrary.MARKER, id, dimx, dimy);

      Element dot = svgp.getIdElement(dotID);
      if (dot != null){
        EventTarget targ = (EventTarget) dot;
        ToolTipListener hoverer = new ToolTipListener(svgp.getDocument(), tooltip.getAttribute("id"));
        targ.addEventListener(SVGConstants.SVG_MOUSEOVER_EVENT_TYPE, hoverer, false);
        targ.addEventListener(SVGConstants.SVG_MOUSEOUT_EVENT_TYPE, hoverer, false);
        targ.addEventListener(SVGConstants.SVG_CLICK_EVENT_TYPE, hoverer, false);
      } else {
        LoggingUtil.message("Attaching ToolTip to non-existing Object: " + dotID);
      }


      layer.appendChild(tooltip);
    }
    return layer;
  }

  @Override
  public int getLevel(){
    return Integer.MAX_VALUE;
  }
}
