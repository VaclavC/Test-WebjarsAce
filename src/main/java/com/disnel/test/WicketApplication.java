package com.disnel.test;

import org.apache.wicket.markup.html.IPackageResourceGuard;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;

import de.agilecoders.wicket.webjars.WicketWebjars;

/**
 * Application object for your web application.
 * If you want to run this application without deploying, run the Start class.
 * 
 * @see com.disnel.test.Start#main(String[])
 */
public class WicketApplication extends WebApplication
{
	/**
	 * @see org.apache.wicket.Application#getHomePage()
	 */
	@Override
	public Class<? extends WebPage> getHomePage()
	{
		return HomePage.class;
	}

	/**
	 * @see org.apache.wicket.Application#init()
	 */
	@Override
	public void init()
	{
		super.init();
		
		WicketWebjars.install(this);
		
		getResourceSettings().setPackageResourceGuard(new IPackageResourceGuard()
		{
			@Override
			public boolean accept(String absolutePath)
			{
				return true;
			}
		});
	}
	
}
