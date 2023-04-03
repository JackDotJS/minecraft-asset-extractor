// these will be used later
// import Directory from './classes/directory';
import { exlog } from './extractor';
import memory from './memory';


function processEntry(entry: FileSystemEntry) {
  if (entry.isFile && entry instanceof FileSystemFileEntry) {
    return processFSFile(entry);
  }

  if (entry.isDirectory && entry instanceof FileSystemDirectoryEntry) {
    return processFSDirectory(entry);
  }

  return Promise.resolve(null);
}

function processFSFile(entry: FileSystemFileEntry) {
  return new Promise<void>((resolve: Function, reject: Function) => {
    entry.file(
      (file) => {
        memory.files.push(file)
        resolve(entry);
      }, 
      (error) => {
        console.error(error);
        reject(error)
      }
    );
  });
}

function processFSDirectory(directory: FileSystemDirectoryEntry) {
  return new Promise((resolve: Function, reject: Function) => {
    let reader = directory.createReader();

    reader.readEntries(async function(entries) {
      // entries == <FileSystemFileEntry | FileSystemDirectoryEntry>[]

      for (const entry of entries) {
        let result = await processEntry(entry);

        if (!(result instanceof FileSystemDirectoryEntry) && !(result instanceof FileSystemFileEntry)) {
          console.error(`something went wrong`, result);
          return reject(directory);
        }
      }

      resolve(directory);
    });
  });
}


// if we want to be able to process entire folders, we need to work
  // with different properties from the results of user file input
  // based on whether they clicked the browse button, or dragged and
  // dropped a folder, hence the two different types specified in the
  // args of this function. i'd really rather not write two completely
  // different extraction methods, so instead, this function will be
  // responsible for cleaning up this mess and converting it into a 
  // simple array of Files.
  //
  // i fucking love web "standards"
export async function normalize(input: FileList | DataTransferItemList) {

  // reset file list in memory
  memory.files = [];

  if (input instanceof FileList) {
    // this isn't strictly an Array so we have to do this dumbassery
    for (let i = 0; i < input.length; i++) {
      memory.files.push(input[i]);
    };
  }

  if (input instanceof DataTransferItemList) {
    let entry = input[0].webkitGetAsEntry();

    if (entry == null) return;

    await processEntry(entry);
  }

  exlog(`Loaded ${memory.files.length} files.`);
  console.debug(memory.files);

  return memory.files
}