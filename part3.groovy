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

logger = Logger.getLogger("oracle.odi")
logger.setLevel(Level.SEVERE)

logger = Logger.getLogger("org.eclipse")
logger.setLevel(Level.SEVERE)

def listObjects (odiInstance,odiClass,listOfObjects) {
	odiObjects = odiInstance.getTransactionalEntityManager().getFinder(odiClass).findAll().sort{it.name}
	for (Object odiSingleObject: odiObjects) 
		listOfObjects.add(odiSingleObject.getName() + " - " + (odiSingleObject.getClass()==OdiScenario.class? odiSingleObject.getVersion() : "NA") )
}

driver 			= "oracle.jdbc.OracleDriver"

sourceUrl = "jdbc:oracle:thin:@YOUR_SERVER_INFO"
sourceSchema = "DEV_ODI_REPO"
sourceSchemaPwd = "XXXXXXXX"
sourceWorkrep = "WORKREP"
sourceOdiUser = "XXXXXXXX"
sourceOdiUserPwd = "XXXXXXXX"

targetUrl = "jdbc:oracle:thin:@YOUR_SERVER_INFO"
targetSchema = "DEV_ODI_REPO"
targetSchemaPwd = "XXXXXXXX"
targetWorkrep = "WORKREP"
targetOdiUser = "XXXXXXXX"
targetOdiUserPwd = "XXXXXXXX"

sourceMasterInfo = new  MasterRepositoryDbInfo(sourceUrl, driver, sourceSchema, sourceSchemaPwd.toCharArray(), new PoolingAttributes())
sourceWorkInfo = new WorkRepositoryDbInfo(sourceWorkrep, new PoolingAttributes())

sourceOdiInstance = OdiInstance.createInstance(new OdiInstanceConfig(sourceMasterInfo, sourceWorkInfo))
sourceAuth = sourceOdiInstance.getSecurityManager().createAuthentication(sourceOdiUser, sourceOdiUserPwd.toCharArray())
sourceOdiInstance.getSecurityManager().setCurrentThreadAuthentication(sourceAuth)

println("Connected to Source! Yay!")

targetMasterInfo = new  MasterRepositoryDbInfo(targetUrl, driver, targetSchema, targetSchemaPwd.toCharArray(), new PoolingAttributes())
targetWorkInfo = new WorkRepositoryDbInfo(targetWorkrep, new PoolingAttributes())

targetOdiInstance = OdiInstance.createInstance(new OdiInstanceConfig(targetMasterInfo, targetWorkInfo))
targetAuth = targetOdiInstance.getSecurityManager().createAuthentication(targetOdiUser, targetOdiUserPwd.toCharArray())
targetOdiInstance.getSecurityManager().setCurrentThreadAuthentication(targetAuth)

println("Connected to Target! Yay!")

try {
	println("Creating Source Scenarios List")
	sourceScenarios = []
	listObjects (sourceOdiInstance,OdiScenario.class,sourceScenarios)
	
	println("Creating Target Scenarios List")
	targetScenarios = []
	listObjects (targetOdiInstance,OdiScenario.class,targetScenarios)
	
	diffScenarios = (sourceScenarios-targetScenarios).findAll(){sourceScenarios -> sourceScenarios.toUpperCase().startsWith('TEST')}
	println("The difference (with the filter) is:")
	println(diffScenarios)
}
catch (e){
	println(e)
}
