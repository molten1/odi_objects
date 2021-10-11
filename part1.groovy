import java.util.logging.Logger
import java.util.logging.Level
import oracle.odi.core.OdiInstance
import oracle.odi.core.config.MasterRepositoryDbInfo
import oracle.odi.core.config.OdiInstanceConfig
import oracle.odi.core.config.PoolingAttributes
import oracle.odi.core.config.WorkRepositoryDbInfo
import oracle.odi.core.security.Authentication
 
logger = Logger.getLogger("oracle.jdbc")
logger.setLevel(Level.SEVERE)
 
sourceUrl = "jdbc:oracle:thin:@YOUR_SERVER_INFO"
driver = "oracle.jdbc.OracleDriver"
sourceSchema = "DEV_ODI_REPO"
sourceSchemaPwd = "XXXXXXXX"
sourceWorkrep = "WORKREP"
sourceOdiUser = "XXXXXXXX"
sourceOdiUserPwd = "XXXXXXXX"

sourceMasterInfo = new MasterRepositoryDbInfo(sourceUrl, driver, sourceSchema, sourceSchemaPwd.toCharArray(), new PoolingAttributes())
sourceWorkInfo = new WorkRepositoryDbInfo(sourceWorkrep, new PoolingAttributes())
 
sourceOdiInstance = OdiInstance.createInstance(new OdiInstanceConfig(sourceMasterInfo, sourceWorkInfo))
sourceAuth = sourceOdiInstance.getSecurityManager().createAuthentication(sourceOdiUser, sourceOdiUserPwd.toCharArray())
sourceOdiInstance.getSecurityManager().setCurrentThreadAuthentication(sourceAuth)
 
println("Connected to ODI! Yay!")