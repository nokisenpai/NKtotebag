package be.noki_senpai.NKtotebag.utils;

import be.noki_senpai.NKmanager.data.DBAccess;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class SQLConnect 
{
	private static HikariConfig jdbcConfig = new HikariConfig();
	private static HikariDataSource ds = null;

	public static HikariDataSource getHikariDS() 
	{
		if(ds.isClosed())
		{
			ds = new HikariDataSource(jdbcConfig);
		}
		return ds;
	}

	public static void setInfo(DBAccess dbAccess)
	{
		jdbcConfig.setPoolName("NKtotebag");
		jdbcConfig.setMaximumPoolSize(10);
		jdbcConfig.setMinimumIdle(2);
		jdbcConfig.setMaxLifetime(900000);
		jdbcConfig.setJdbcUrl("jdbc:mysql://" + dbAccess.getHost() + ":" + dbAccess.getPort() + "/" + dbAccess.getDbName() + "?useSSL=false&autoReconnect=true&useUnicode=yes");
		jdbcConfig.setUsername(dbAccess.getUser());
		jdbcConfig.setPassword(dbAccess.getPassword());
		ds = new HikariDataSource(jdbcConfig);
	}
}