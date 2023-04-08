import { exlog } from './extractor';
import memory from './memory';

export interface MAEFile {
  name: string,
  data: Blob | File,
  path: string
}

// FileSystemEntry

function processFSEntry(entry: FileSystemEntry) {
  if (entry.isFile && entry instanceof FileSystemFileEntry) {
    return processFileEntry(entry);
  }

  if (entry.isDirectory && entry instanceof FileSystemDirectoryEntry) {
    return processDirectoryEntry(entry);
  }

  return Promise.resolve(null);
}

function processFileEntry(entry: FileSystemFileEntry) {
  return new Promise<void>((resolve: Function, reject: Function) => {
    entry.file(
      (file) => {
        let nFile: MAEFile = {
          name: file.name,
          data: file,
          path: file.webkitRelativePath
        }

        memory.files.push(nFile);
        resolve(entry);
      }, 
      (error) => {
        console.error(error);
        reject(error)
      }
    );
  });
}

function processDirectoryEntry(directory: FileSystemDirectoryEntry) {
  return new Promise((resolve: Function, reject: Function) => {
    let reader = directory.createReader();

    reader.readEntries(async function(entries) {
      // entries == <FileSystemFileEntry | FileSystemDirectoryEntry>[]

      for (const entry of entries) {
        let result = await processFSEntry(entry);

        if (!(result instanceof FileSystemDirectoryEntry) && !(result instanceof FileSystemFileEntry)) {
          console.error(`something went wrong`, result);
          return reject(directory);
        }
      }

      resolve(directory);
    });
  });
}

// FileSystemHandle

function processFSHandle(entry: FileSystemHandle, currentPath?: string) {
  if (!currentPath) {
    currentPath = entry.name;
  } else {
    // TODO: we may need platform-specific path seperators
    currentPath = `${currentPath}/${entry.name}`;
  }

  if (entry.kind == `file` && entry instanceof FileSystemFileHandle) {
    return processFileHandle(entry, currentPath);
  }

  if (entry.kind == `directory` && entry instanceof FileSystemDirectoryHandle) {
    return processDirectoryHandle(entry, currentPath);
  }

  return Promise.resolve(null);
}

async function processFileHandle(entry: FileSystemFileHandle, currentPath: string) {
  let file = await entry.getFile();

  let nFile: MAEFile = {
    name: file.name,
    data: file,
    path: currentPath
  }

  memory.files.push(nFile);
  return file;
}

async function processDirectoryHandle(directory: FileSystemDirectoryHandle, currentPath: string) {
  // @ts-ignore
  for await (const entry of directory.values()) {
    // VERY LAGGY WITH LARGE AMOUNTS OF FILES!!!!
    // console.debug(entry);

    await processFSHandle(entry, currentPath);
  }
}

/**
 * if we want to be able to process entire folders, we need to work
 * with different properties from the results of user file input
 * based on whether they clicked the browse button, or dragged and
 * dropped a folder, hence the two different types specified in the
 * args of this function. just to make things worse, chrome has
 * decided to ditch the File and Directory Entries API *completely*,
 * so now i need an additional processing method for the bullshit
 * they decided to put in place of that. now with all that said,
 * this function will be responsible for cleaning up this mess and 
 * converting it into a simple array of MAEFiles, which will provide
 * an actually consistent format which we can work with.
 * 
 * i fucking love web "standards"
 */
export async function normalize(input: FileList | DataTransferItemList) {
  exlog(`New files received. Just a moment...`);

  // reset file list in memory
  memory.files = [];

  if (input instanceof FileList) {
    // this isn't strictly an Array so we have to do this dumbassery
    for (let i = 0; i < input.length; i++) {

      let nFile: MAEFile = {
        name: input[i].name,
        data: input[i],
        path: input[i].webkitRelativePath
      }

      memory.files.push(nFile);
    };
  }

  if (input instanceof DataTransferItemList) {
    let entry = input[0].webkitGetAsEntry();
    let entryHandler : Function = processFSEntry;

    // @ts-ignore
    if (input[0].getAsFileSystemHandle != null) {
      // chromium only
      // @ts-ignore
      entry = await input[0].getAsFileSystemHandle();
      entryHandler = processFSHandle;
    }

    if (entry == null) return console.log(`invalid`);

    await entryHandler(entry);
  }

  memory.files.sort((a, b) => { return a.path.localeCompare(b.path); });

  exlog(`Loaded ${memory.files.length} files.`);
  console.debug(memory.files);

  return memory.files
}

export function getFile(path: string) {
  for (const file of memory.files) {
    if (file.path == path) return file;
  }

  return null
}

export function getFilesInDirectory(path: string) {
  if (path.endsWith(`\\`)) path = path.substring(path.length-1);

  // TODO: we may need platform-specific path seperators
  let pattern = new RegExp(`${path}/(?!.*/.*)`);
  let found: MAEFile[] = [];

  for (const file of memory.files) {
    if (pattern.test(file.path)) found.push(file);
  }

  return found;
}