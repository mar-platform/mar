package mar.bpmn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

/**
 * https://github.com/camunda/camunda-bpm-platform/tree/master/model-api/bpmn-model
 * https://github.com/camunda/camunda-bpm-examples/tree/master/bpmn-model-api/parse-bpmn
 * 
 * @author jesus
 *
 */
public class TestImporter {

	public static void main(String[] args) throws FileNotFoundException {
		BpmnModelInstance modelInstance = Bpmn.readModelFromStream(new FileInputStream("/home/jesus/projects/mde-ml/MAR/mar-bpmn/examples/example1.bpmn"));
		
	}
	
}
