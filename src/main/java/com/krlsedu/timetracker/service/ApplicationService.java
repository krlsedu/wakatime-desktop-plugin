package com.krlsedu.timetracker.service;

import com.krlsedu.timetracker.model.Application;
import com.sun.jna.platform.win32.WinDef;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ApplicationService {
	private static WinDef.HWND prevForegroundWindow = null;
	private static Application aplication = null;
	
	public static void generateApplication(WinDef.HWND foregroundWindow) throws Exception {
		if (!foregroundWindow.equals(prevForegroundWindow)) {
			if (prevForegroundWindow != null) {
				aplication.setDateEnd(new Date());
				aplication.setTimeSpentMillis(aplication.getDateEnd().getTime() - aplication.getDateIni().getTime());
				aplication.setOsName(SystenInfo.getOsName());
				aplication.setHostName(SystenInfo.getHostName());
				Sender.post("http://192.168.0.8:8080/api/v1/log-application", Sender.getObjectMapper().writeValueAsString(aplication));
				System.out.println(aplication);
				aplication = new Application();
				aplication.setName(User32DLL.getImageName(foregroundWindow));
				aplication.setDateIni(new Date());
			} else {
				aplication = new Application();
				aplication.setName(User32DLL.getImageName(foregroundWindow));
				aplication.setDateIni(new Date());
			}
			prevForegroundWindow = foregroundWindow;
		}
	}
}
