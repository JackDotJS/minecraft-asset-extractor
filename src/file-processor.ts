// these will be used later
// import MAEDirectory from './classes/directory';
// import memory from './memory';


export function normalize(input: FileList | DataTransferItemList) {
  // TODO: moves the files to memory with our own data structure
  //
  // if we want to be able to process entire folders, we need to work
  // with different properties from the results of user file input
  // based on whether they clicked the browse button, or dragged and
  // dropped a folder, hence the two different types specified in the
  // args of this function. i'd really rather not write two completely
  // different extraction methods, so instead, this function will be
  // responsible for cleaning up this mess and converting it into my
  // own structure. it just needs to be something usable regardless
  // of how the files initially came through.
  //
  // i fucking love web "standards"

  console.debug(typeof input, input);
}