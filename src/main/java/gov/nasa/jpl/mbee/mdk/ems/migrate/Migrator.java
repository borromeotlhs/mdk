package gov.nasa.jpl.mbee.mdk.ems.migrate;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.ProjectUtilities;
import com.nomagic.task.ProgressStatus;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdmodels.Model;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import gov.nasa.jpl.mbee.mdk.MDKPlugin;
import gov.nasa.jpl.mbee.mdk.ems.ExportUtility;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.OutputQueue;
import gov.nasa.jpl.mbee.mdk.ems.sync.queue.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

@Deprecated
//TODO purge? @donbot
public abstract class Migrator {

    private Project project;
    protected Set<Element> missing = new HashSet<Element>();

    public Migrator() {
        project = Application.getInstance().getProject();
        for (Element elem : project.getModel().getOwnedElement()) {
            getAllMissing(elem, missing);
        }
    }

    public void migrate(ProgressStatus ps) {
    }

    public static void commit(JSONArray elements) {
        JSONObject send = new JSONObject();
        send.put("elements", elements);
        send.put("source", "magicdraw");
        send.put("mmsVersion", MDKPlugin.VERSION);


        String url = ExportUtility.getPostElementsUrl();
        if (url == null) {
            return;
        }

        // try to do a server background commit
        //url += "?background=true"; added in Request

        Application.getInstance().getGUILog().log("[INFO] Migration request is added to queue.");
        OutputQueue.getInstance().offer(new Request(url, send.toJSONString(), elements.size(), "Migration", false));
    }


    /**
     * here's the code to get all the elements in the project (in focus)
     */
    private void getAllMissing(Element current, Set<Element> missing) {
        if (ProjectUtilities.isElementInAttachedProject(current)) {
            return;
        }
        if (!ExportUtility.shouldAdd(current)) {
            return;
        }
        if (!(current instanceof Model && ((Model) current).getName().equals("Data"))) {
            missing.add(current);
        }
        for (Element e : current.getOwnedElement()) {
            getAllMissing(e, missing);
        }
    }
}