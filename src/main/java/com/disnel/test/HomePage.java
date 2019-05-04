package com.disnel.test;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import de.agilecoders.wicket.webjars.request.resource.WebjarsJavaScriptResourceReference;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebPage;

public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	private Component editor;
	private String mode = "html";

	private IModel<String> contentModel;
	
	private static final String DEFAULT_CONTENT =
			"<html>\n"
			+ "\t<body>Some content</body>\n"
			+ "</html>";
	
	public HomePage(final PageParameters parameters)
	{
		super(parameters);

		add(editor = new Label("edit", contentModel = new Model<String>(DEFAULT_CONTENT)));
		editor.add(new AjaxAceBehavior()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSave(AjaxRequestTarget target, String content)
			{
				contentModel.setObject(content);
				
				System.out.println("Saved");
			}
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(
				new WebjarsJavaScriptResourceReference("ace-builds/src-min-noconflict/ace.js")));
		
		response.render(OnDomReadyHeaderItem.forScript(jsStartEditor(true)));
	}
	
	private String jsEditorVar(String componentMId)
	{
		return componentMId + "Editor";
	}
	
	private String jsStartEditor(boolean editable)
	{
		StringBuilder sb = new StringBuilder();
		
		String editorMId = editor.getMarkupId();
		String editorVar = jsEditorVar(editorMId);
		String acePath = urlFor(new WebjarsJavaScriptResourceReference("ace-builds/src-min-noconflict"), new PageParameters()).toString();
		
		sb.append("ace.config.set('basePath', '");
		sb.append(acePath);
		sb.append("');");
		
		sb.append(String.format("%s = ace.edit('%s', {", editorVar, editorMId));
		sb.append("fontSize: 'medium',");
		sb.append("maxLines: Infinity,");
		sb.append("autoScrollEditorIntoView: true,");
		sb.append("wrap: true,");
		if ( !editable )
			sb.append("readOnly: true,");
		sb.append("});");

		sb.append(editorVar);
		sb.append(".commands.addCommand({");
		sb.append("name: 'saveFile',");
		sb.append("bindKey: { win: 'Ctrl-S', mac: 'Command-S', sender: 'editor|cli'},");
		sb.append("exec: function(env, args, request) {");
		sb.append(String.format("$('#%s').trigger('ace:save', %s.getValue());", editorMId, editorVar));
		sb.append("}");
		sb.append("});");
		
		sb.append(editorVar);
		sb.append(".setTheme('ace/theme/github');");

		if ( mode != null )
		{
			sb.append(editorVar);
			sb.append(".session.setMode('ace/mode/" + mode + "');");
		}
		
		return sb.toString();
	}
	
	private abstract class AjaxAceBehavior extends AbstractDefaultAjaxBehavior
	{
		private static final long serialVersionUID = 1L;

		public static final String CALLBACK_ATTR = "data-ace-callback";
		
		@Override
		protected void respond(AjaxRequestTarget target)
		{
			RequestCycle requestCycle = RequestCycle.get();
			IRequestParameters params = requestCycle.getRequest().getRequestParameters();
			
			StringValue cmdValue = params.getParameterValue("cmd");
			if ( cmdValue != null )
			{
				if ( "save".equals(cmdValue.toString()) )
				{
					StringValue content = params.getParameterValue("content");
					
					onSave(target, content.toString());
				}
			}
		}
		
		@Override
		protected void onComponentTag(ComponentTag tag)
		{
			tag.put(CALLBACK_ATTR, getCallbackUrl().toString());
		}
		
		@Override
		public void renderHead(Component component, IHeaderResponse response)
		{
			super.renderHead(component, response);
			
			String cmId = getComponent().getMarkupId();
			
			response.render(OnDomReadyHeaderItem.forScript(
					String.format("$('#%s').on('ace:save', function() { "
							+ "Wicket.Ajax.post({'u': $(%s).attr('%s'), 'ep': { cmd: 'save', content: %s.getValue() }});"
							+ " });",
							cmId, cmId, CALLBACK_ATTR, jsEditorVar(cmId))));
		}
		
		protected abstract void onSave(AjaxRequestTarget target, String content);
	}
	
}
