import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class Monitor {
	
	private static final int MAX_TITLE_LENGTH = 1024;
	
	public static void main(String[] args) throws Exception {
		WinDef.HWND prevFg = null;
		
		while (true) {
			Thread.sleep(200);
			
			WinDef.HWND fg = User32.INSTANCE.GetForegroundWindow();
			
			if (fg != null && fg.equals(prevFg)) {
				continue;
			}
			
			String fgImageName = getImageName(fg);
			if (fgImageName == null) {
				System.out.println("Failed to get the image name!");
			} else {
				System.out.println(fgImageName);
				char[] buffer = new char[MAX_TITLE_LENGTH * 2];
				User32DLL.GetWindowTextW(User32DLL.GetForegroundWindow(), buffer, MAX_TITLE_LENGTH);
				System.out.println("Active window title: " + Native.toString(buffer));
			}
			
			prevFg = fg;
		}
		
	}
	
	private static String getImageName(WinDef.HWND window) {
		// Get the process ID of the window
		IntByReference procId = new IntByReference();
		User32.INSTANCE.GetWindowThreadProcessId(window, procId);
		
		// Open the process to get permissions to the image name
		WinNT.HANDLE procHandle = Kernel32.INSTANCE.OpenProcess(
				Kernel32.PROCESS_QUERY_LIMITED_INFORMATION,
				false,
				procId.getValue()
		);
		
		// Get the image name
		char[] buffer = new char[4096];
		IntByReference bufferSize = new IntByReference(buffer.length);
		boolean success = Kernel32.INSTANCE.QueryFullProcessImageName(procHandle, 0, buffer, bufferSize);
		
		// Clean up: close the opened process
		Kernel32.INSTANCE.CloseHandle(procHandle);
		
		return success ? new String(buffer, 0, bufferSize.getValue()) : null;
	}
	
	static class Psapi {
		static {
			Native.register("psapi");
		}
		
		public static native int GetModuleBaseNameW(Pointer hProcess, Pointer hmodule, char[] lpBaseName, int size);
	}
	
	static class User32DLL {
		static {
			Native.register("user32");
		}
		
		public static native int GetWindowThreadProcessId(WinDef.HWND hWnd, PointerByReference pref);
		
		public static native WinDef.HWND GetForegroundWindow();
		
		public static native int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);
	}
}