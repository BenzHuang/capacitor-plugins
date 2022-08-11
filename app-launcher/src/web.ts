import { WebPlugin } from '@capacitor/core';

import type {
	AppLauncherPlugin,
	CanOpenURLOptions,
	CanOpenURLResult,
	OpenFileOptions,
	OpenURLOptions,
	OpenURLResult,
} from './definitions';

export class AppLauncherWeb extends WebPlugin implements AppLauncherPlugin {
  async canOpenUrl(_options: CanOpenURLOptions): Promise<CanOpenURLResult> {
    return { value: true };
  }

  async openUrl(options: OpenURLOptions): Promise<OpenURLResult> {
    window.open(options.url, '_blank');
    return { completed: true };
  }

  async	openWpsApp(_options: OpenFileOptions): Promise<OpenURLResult> {
	  return { completed: true };
  }
}
