/**
 * Get an environment variable, no matter which environment you are using.
 * @param key environment variable name; see [config.js](../../public/config.js)
 * @param value (optional) default value, used when all else fails
 */
export default function getEnv(key: string, value?: string | undefined): string {
  return import.meta.env[key] ?? (window as unknown as { config: { [index: string]: string | undefined } }).config[key] ?? value;
}
