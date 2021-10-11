import java.util.logging.Logger
import javax.swing.tree.DefaultMutableTreeNode
import java.util.logging.Level
import oracle.odi.core.OdiInstance;
import oracle.odi.core.config.MasterRepositoryDbInfo
import oracle.odi.core.config.OdiInstanceConfig
import oracle.odi.core.config.PoolingAttributes
import oracle.odi.core.config.WorkRepositoryDbInfo
import oracle.odi.core.security.Authentication
import oracle.odi.domain.runtime.loadplan.OdiLoadPlan
import oracle.odi.domain.runtime.scenario.OdiScenario
import oracle.odi.domain.runtime.scenario.OdiScenarioFolder
import oracle.odi.domain.runtime.scenario.finder.IOdiScenarioFinder
import oracle.odi.domain.runtime.scenario.finder.IOdiScenarioFolderFinder

logger = Logger.getLogger("oracle.jdbc")
logger.setLevel(Level.SEVERE)

def listObjects (odi,odiClass) {
	odiObjects = odi.getTransactionalEntityManager().getFinder(odiClass).findAll().sort{it.name}
	if (odiObjects.size() > 0) {
		for (int i = 0; i < odiObjects.size(); i++) {
			odiSingleObject = odiObjects.toArray()[i]
			println(odiSingleObject.getName() + " - " + (odiSingleObject.getClass()==OdiScenario.class? odiSingleObject.getVersion() : "NA") )
		}
	}
}

sourceUrl = "jdbc:oracle:thin:@YOUR_SERVER_INFO"
driver = "oracle.jdbc.OracleDriver"
sourceSchema = "DEV_ODI_REPO"
sourceSchemaPwd = "XXXXXXXX"
sourceWorkrep = "WORKREP"
sourceOdiUser = "XXXXXXXX"
sourceOdiUserPwd = "XXXXXXXX"

sourceMasterInfo = new  MasterRepositoryDbInfo(sourceUrl, driver, sourceSchema, sourceSchemaPwd.toCharArray(), new PoolingAttributes())
sourceWorkInfo = new WorkRepositoryDbInfo(sourceWorkrep, new PoolingAttributes())

sourceOdiInstance = OdiInstance.createInstance(new OdiInstanceConfig(sourceMasterInfo, sourceWorkInfo))
sourceAuth = sourceOdiInstance.getSecurityManager().createAuthentication(sourceOdiUser, sourceOdiUserPwd.toCharArray())
sourceOdiInstance.getSecurityManager().setCurrentThreadAuthentication(sourceAuth)

println("Connected to ODI! Yay!")

try {
	listObjects (sourceOdiInstance,OdiScenario.class)
	listObjects (sourceOdiInstance,OdiLoadPlan.class)
	listObjects (sourceOdiInstance,OdiScenarioFolder.class)
}
catch (e){
	println(e)
}
