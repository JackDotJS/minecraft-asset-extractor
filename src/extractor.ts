// will be used later
//import { getFile, getFilesInDirectory } from './file-processor';
import memory from './memory';
import JSZip from 'jszip';

export function exlog(...content: any) {
  console.log(...content);

  const output = memory.outputElem;

  if (output == null) return;

  let autoscroll = false;

  if (output.scrollTop >= (output.scrollHeight - output.clientHeight) - 10) autoscroll = true;

  const now = new Date();

  const h = `${now.getHours()}`.padStart(2, `0`);
  const m = `${now.getMinutes()}`.padStart(2, `0`);
  const s = `${now.getSeconds()}`.padStart(2, `0`);
  const ms = `${now.getMilliseconds()}`.padStart(3, `0`);

  const timestamp = `[${h}:${m}:${s}.${ms}]`;

  let str = "";

  for (const item of content) {
    str += item.toString();
  }

  const tsPadding = `  `

  const strWithPadding = str.replace(/\n/g, `\n${` `.repeat(timestamp.length)}${tsPadding}`);

  output.textContent = `${output.textContent}\n${timestamp}${tsPadding}${strWithPadding}`.trim();

  if (autoscroll) output.scrollTop = output.scrollHeight - output.clientHeight;
}

export function initialize(outputElem: Element | null) {
  if (outputElem != null) memory.outputElem = outputElem;
}

export function extract() {
  exlog(`Beginning extraction...`);
  const startTime = new Date().getTime();
  let processedFiles = 0;
  const zip = new JSZip();

  // this is just a test to make sure things actually work
  // all this does is zip up every file you provide as-is
  for (const file of memory.files) {
    zip.file(file.path, file.data);
    processedFiles++;
  }

  // DO NOT use any of the compression options if you want this to take any reasonable amount of time.
  zip.generateAsync({ type: `blob` }).then(blob => {
    const timeTaken = new Date().getTime() - startTime;
    exlog([
      `=== Extraction Finished ===`,
      `Files Processed: ${processedFiles}`,
      `Final Archive Size: ${((blob.size / 1000) / 1000).toFixed(2)} MB`,
      `Time Elapsed: ${(timeTaken / 1000).toFixed(3)} seconds`
    ].join(`\n`));

    const link = document.createElement(`a`);
    link.href = URL.createObjectURL(blob);
    link.download = `assets.zip`;
    link.style.display = `none`;
    document.body.appendChild(link);

    link.click();

    URL.revokeObjectURL(link.href);
    link.remove();
  }).catch(console.error);
}