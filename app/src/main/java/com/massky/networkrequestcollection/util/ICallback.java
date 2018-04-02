package com.massky.networkrequestcollection.util;

/**
 * 加调接口
 * @author Administrator
 *
 */
public interface ICallback {
	/**
	 * 处理程序
	 * @param data
	 */
	public void process(Object data);
	public void error(String data);
}
