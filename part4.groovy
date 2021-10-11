import java.util.logging.Logger
import javax.swing.tree.DefaultMutableTreeNode
import java.util.logging.Level
import oracle.odi.core.OdiInstance;
import oracle.odi.core.config.MasterRepositoryDbInfo
import oracle.odi.core.config.OdiInstanceConfig
import oracle.odi.core.config.PoolingAttributes
import oracle.odi.core.config.WorkRepositoryDbInfo
import oracle.odi.core.persistence.transaction.ITransactionStatus
import oracle.odi.core.persistence.transaction.support.DefaultTransactionDefinition
import oracle.odi.core.security.Authentication
import oracle.odi.domain.runtime.loadplan.OdiLoadPlan
import oracle.odi.domain.runtime.scenario.OdiScenario
import oracle.odi.domain.runtime.scenario.OdiScenarioFolder
import oracle.odi.domain.runtime.scenario.finder.IOdiScenarioFinder
import oracle.odi.domain.runtime.scenario.finder.IOdiScenarioFolderFinder
import oracle.odi.impexp.EncodingOptions
import oracle.odi.impexp.support.ExportServiceImpl
import oracle.odi.impexp.support.ImportServiceImpl

logger = Logger.getLogger("oracle.jdbc")
logger.setLevel(Level.SEVERE)

logger = Logger.getLogger("oracle.odi")
logger.setLevel(Level.SEVERE)

logger = Logger.getLogger("org.eclipse")
logger.setLevel(Level.SEVERE)

def listObjects (odiInstance,odiClass,listOfObjects) {
	odiObjects = odiInstance.getTransactionalEntityManager().getFinder(odiClass).findAll().sort{it.name}
	for (Object odiSingleObject: odiObjects) 
		listOfObjects.add(odiSingleObject)
}

driver 			= "oracle.jdbc.OracleDriver"

sourceUrl 		= "jdbc:oracle:thin:@YOUR_SERVER_INFO"
sourceSchema 	= "DEV_ODI_REPO"
sourceSchemaPwd = "XXXXXXXX"
sourceWorkrep 	= "WORKREP"
sourceOdiUser	= "XXXXXXXX"
sourceOdiUserPwd = "XXXXXXXX"

targetUrl 		= "jdbc:oracle:thin:@YOUR_SERVER_INFO"
targetSchema 	= "DEV_ODI_REPO"
targetSchemaPwd = "SXXXXXXXX"
targetWorkrep 	= "WORKREP"
targetOdiUser	= "XXXXXXXX"
targetOdiUserPwd = "XXXXXXXX"

exportPath = "C:\\Odi"

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

	diffScenarios = []
	for (Object odiSingleObject: sourceScenarios)
		if (targetScenarios.find {targetScenarios -> targetScenarios.getName() == odiSingleObject.getName() && targetScenarios.getVersion() == odiSingleObject.getVersion()}.equals(null)) 
			if (odiSingleObject.getName().startsWith('TEST'))
				diffScenarios.add(odiSingleObject)
		
	println("List of ODI Scenarios that will be migrated")
		for (Object singObject: diffScenarios)
			println(singObject.getName() + "_" + singObject.getVersion())
			
	encode = new EncodingOptions();
		
	transSource = sourceOdiInstance.getTransactionManager().getTransaction(new DefaultTransactionDefinition());
	exportService = new ExportServiceImpl(sourceOdiInstance);
	for (Object singObject: diffScenarios)
		exportService.exportToXml(singObject, exportPath, true, true, encode)
			
	tm = targetOdiInstance.getTransactionManager()
	transTarget = tm.getTransaction(new DefaultTransactionDefinition());
	importService = new ImportServiceImpl(targetOdiInstance);
	for (Object singObject: diffScenarios)
	{
		println(exportPath+"\\SCEN_"+singObject.getName() + "_Version_" + singObject.getVersion()+".xml")
		importService.importObjectFromXml(ImportServiceImpl.IMPORT_MODE_SYNONYM_INSERT_UPDATE,exportPath+"\\SCEN_"+singObject.getName() + "_Version_" + singObject.getVersion()+".xml", true, null, true)
	}	
	tm.commit(transTarget)
							
}
catch (e){
	println(e)
}