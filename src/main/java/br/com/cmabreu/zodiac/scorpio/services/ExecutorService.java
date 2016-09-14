package br.com.cmabreu.zodiac.scorpio.services;

import java.util.List;

import br.com.cmabreu.zodiac.scorpio.entity.ActivationExecutor;
import br.com.cmabreu.zodiac.scorpio.exceptions.DatabaseConnectException;
import br.com.cmabreu.zodiac.scorpio.exceptions.NotFoundException;
import br.com.cmabreu.zodiac.scorpio.repository.ExecutorRepository;
import br.com.cmabreu.zodiac.scorpio.types.ExecutorType;

public class ExecutorService {
	private ExecutorRepository rep;

	public String getAsManifest() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<manifest>\n");
		List<ActivationExecutor> preList = null;
		try {
			preList = rep.getList();
		} catch ( NotFoundException e ) {
			
		}
		
		sb.append("\t<wrapper activity=\"r-wrapper.jar\" name=\"RRUNNER\" type=\"SYSTEM\" hash=\"RWRAPPER\" target=\"ANY\" version=\"1.0\" />\n");
		
		for ( ActivationExecutor executor :  preList  ) {
			ExecutorType type = executor.getType();
			if ( type != ExecutorType.SELECT ) {
				String alias = executor.getExecutorAlias();
				String wrapper = executor.getActivationWrapper();
				String hash = executor.getHash();
				sb.append("\t<wrapper activity=\""+wrapper+"\" name=\""+ alias +"\" type=\"" + type.toString() + "\" hash=\""+hash+"\" target=\"ANY\" version=\"1.0\" />\n");
			}
		}
		sb.append("</manifest>\n\n");
		return sb.toString();
	}
	
	public ExecutorService() throws DatabaseConnectException {
		this.rep = new ExecutorRepository();
	}

	public ActivationExecutor getExecutor(int idExecutor) throws NotFoundException{
		return rep.getActivationExecutor(idExecutor);
	}

	public ActivationExecutor getExecutor(String executorAlias) throws NotFoundException{
		return rep.getActivationExecutor(executorAlias);
	}
	
	public void newTransaction() {
		if ( !rep.isOpen() ) {
			rep.newTransaction();
		}
	}
	
	
	public List<ActivationExecutor> getList() throws NotFoundException {
		List<ActivationExecutor> preList = rep.getList();
		return preList;	
	}
	
}
