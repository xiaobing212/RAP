package com.baidu.rigel.rap.project.web.action;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baidu.rigel.rap.account.bo.User;
import com.baidu.rigel.rap.auto.generate.bo.VelocityTemplateGenerator;
import com.baidu.rigel.rap.auto.generate.contract.Generator;
import com.baidu.rigel.rap.common.ActionBase;
import com.baidu.rigel.rap.project.bo.Page;
import com.baidu.rigel.rap.project.bo.Project;
import com.baidu.rigel.rap.project.service.ProjectMgr;
import com.baidu.rigel.rap.workspace.service.WorkspaceMgr;

public class ProjectAction extends ActionBase {
	
	private int id;
	
	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public List<String> getMemberAccountList() {
		List<String> memberList = new ArrayList<String>();
		for (User user : super.getAccountMgr().getUserList()) {
			memberList.add(user.getAccount() + "(" + user.getName() + ")");
		}		
		return memberList;
	}
	
	private String memberAccountListStr;
	
	public String getMemberAccountListStr() {
		return memberAccountListStr;
	}
	
	public void setMemberAccountListStr(String memberAccountListStr) {
		this.memberAccountListStr = memberAccountListStr;
	}
	
	private String name;
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	private Date createDate;
	
	public Date getCreateDate() {
		return this.createDate;
	}
	
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	private String introduction;
	
	public String getIntroduction() {
		return this.introduction;
	}
	
	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
	
	private List<Project> projectList;
	
	public List<Project> getProjectList() {
		return this.projectList;
	}
	
	public void setProjectList(List<Project> projectList) {
		this.projectList = projectList;
	}
	
	private Project project;
	
	public Project getProject() {
		return this.project;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	private static final long serialVersionUID = 1L;
	
	private ProjectMgr projectMgr;
	
	/**
	 * get project manager
	 * @return project manager
	 */
	public ProjectMgr getProjectMgr() {
		return projectMgr;
	}
	
	/**
	 * set project manager
	 * @param projectMgr project manager
	 * @throws exceptions No exceptions thrown
	 */
	public void setProjectMgr(ProjectMgr projectMgr) {
		this.projectMgr = projectMgr;
	}
	
	private WorkspaceMgr workspaceMgr;
	
	public WorkspaceMgr getWorkspaceMgr() {
		return workspaceMgr;
	}
	
	public void setWorkspaceMgr(WorkspaceMgr workspaceMgr) {
		this.workspaceMgr = workspaceMgr;
	}
	
	private int pageId;
	
	public int getPageId() {
		return pageId;
	}
	
	public void setPageId(int pageId) {
		this.pageId = pageId;
	}
	
	private String projectData;
	
	public String getProjectData() {
		return projectData;
	}
	
	public void setProjectData(String projectData) {
		this.projectData = projectData;
	}
	
	/**
	 * remove project
	 * @param id{int} id of the project to be removed
	 */	
	public String removeProject(){
		if (!isUserLogined()) return LOGIN;
		if (!getCurUser().canManageProject(getId())) {
			setErrMsg("您没有管理该项目的权限");
			return ERROR;
		}
		projectMgr.removeProject(getId());
		return myProjectList();
	}
	
	private String addProject() {
		if (!isUserLogined()) return LOGIN;
		Project project = new Project();
		project.setCreateDate(new Date());
		project.setUser(getCurUser());
		project.setIntroduction(getIntroduction());
		project.setName(getName());
		List<String> memberAccountList = new ArrayList<String>();
		String[] list = getMemberAccountListStr().split(",");
		// format:   mashengbo(大灰狼堡森), linpanhui(林攀辉),
		for (String item : list) {
			String account = item.contains("(") ? 
					item.substring(0, item.indexOf("(")).trim() 
					: 
					item.trim();
			if (!account.equals(""))
				memberAccountList.add(account);
		}	
		project.setMemberAccountList(memberAccountList);
		projectMgr.addProject(project);
		return this.myProjectList();
	}
	
	private String updateProject() {
		if (!isUserLogined()) return LOGIN;
		if (!getCurUser().canManageProject(getId())) {
			setErrMsg("您没有管理该项目的权限");
			return ERROR;
		}
		Project project = new Project();
		project.setId(getId());
		project.setIntroduction(getIntroduction());
		project.setName(getName());
		
		List<String> memberAccountList = new ArrayList<String>();
		String[] list = getMemberAccountListStr().split(",");
		// format:   mashengbo(大灰狼堡森), linpanhui(林攀辉),
		for (String item : list) {
			String account = item.contains("(") ? 
					item.substring(0, item.indexOf("(")).trim() 
					: 
					item.trim();
			if (!account.equals(""))
				memberAccountList.add(account);
		}			
		project.setMemberAccountList(memberAccountList);
		projectMgr.updateProject(project);
		return myProjectList();
	}
	
	public String addOrUpdateProject() {
		if(id == 0) {
			return addProject();
		} else {
			return updateProject();
		}
	}
	
	public String myProjectList() {
		if (!isUserLogined()) return LOGIN;
		
		// query paging
		super.initPager();
		int curPageNum = super.getPager().getCurPagerNum();
		int pageSize = super.getPager().getPagerSize();
		long totalRecNum = projectMgr.getProjectListNum(getCurUser());
		getPager().setTotalRecNum(totalRecNum);
		
		setProjectList(projectMgr.getProjectList(getCurUser(), curPageNum, pageSize));
		return SUCCESS;
	}	
	
	public String myProject() {
		if (!isUserLogined()) return LOGIN;
		if (!getCurUser().haveAccessOfProject(getId())) {
			setErrMsg("你没有访问该项目的权限。");
			return ERROR;
		}
		setProject(projectMgr.getProject(getId()));
		setJson(getProject().toString(Project.toStringType.TO_PAGE));
		return SUCCESS;
	}
	
	public String projectDetailAjax() {
		if (!isUserLogined()) return LOGIN;
		if (!getCurUser().haveAccessOfProject(getId())) {
			setErrMsg("你没有访问该项目的权限。");
			return ERROR;
		}
		return this.myProject();
	}
	
	private String result;
	
	public String getResult() {
		return result;
	}
	
	public void setResult(String result) {
		this.result = result;
	}	
	
	
	
	private InputStream outputStream;
	
	public InputStream getOutputStream() {
		return outputStream;
	}
	
	public void setOutputStream(InputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public String autoGenerate() throws FileNotFoundException, UnsupportedEncodingException {
		Generator generator = new VelocityTemplateGenerator();
		Page page = projectMgr.getPage(getPageId());
		generator.setObject(page);
		String exportFileString = generator.doGenerate();
		outputStream = new ByteArrayInputStream(exportFileString.getBytes("UTF8"));
		return SUCCESS;
	}
	
	public String getGeneratedFileName() {
		return projectMgr.getPage(getPageId()).getTemplate();
	}
	
	
}
