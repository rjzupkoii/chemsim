package edu.mtu.simulation.chart;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;

import org.jfree.data.xy.XYSeries;

import edu.mtu.simulation.CompoundInspector;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.media.chart.ChartGenerator;
import sim.util.media.chart.TimeSeriesAttributes;
import sim.util.media.chart.TimeSeriesChartGenerator;

@SuppressWarnings("serial")
public class HydroxylRadicalChart implements Steppable {
	
	XYSeries acetone = new XYSeries("Acetone");
	XYSeries hydrogenPeroxide = new XYSeries("Hydrogen Peroxide");
	XYSeries hydroxylRadical = new XYSeries("Hydroxyl Radical");
	
	TimeSeriesChartGenerator chart;

	@Override
	public void step(SimState state) {
		
		CompoundInspector inspector = new CompoundInspector();
		double time = state.schedule.getTime();
		
		acetone.add(time, inspector.getAcetoneCount());
		hydrogenPeroxide.add(time, inspector.getHydrogenPeroxideCount());
		hydroxylRadical.add(time, inspector.getHydroxylRadicalCount());
		
		chart.update(ChartGenerator.FORCE_KEY, true);
	}
	
	public JFrame createFrame() {
		// Prepare the chart that is displayed
		chart = new TimeSeriesChartGenerator();
		chart.setTitle("Hydroxyl Radical Production");
		chart.setXAxisLabel("Time Step");
		chart.setYAxisLabel("Entity Count");
		((TimeSeriesAttributes)(chart.addSeries(hydrogenPeroxide, null))).setStrokeColor(Color.RED);
        ((TimeSeriesAttributes)(chart.addSeries(hydroxylRadical, null))).setStrokeColor(Color.GREEN);
        ((TimeSeriesAttributes)(chart.addSeries(acetone, null))).setStrokeColor(Color.BLUE);

        // Prepare the frame that the chart is displayed in
        JFrame frame = chart.createFrame();
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(chart, BorderLayout.CENTER);
        frame.pack();
        
        return frame;
	}
}
