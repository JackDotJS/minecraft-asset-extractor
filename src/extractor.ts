interface opts {
  outputElem: Element | null,
  logContent: string
}

const opts: opts = {
  outputElem: null,
  logContent: ""
}

export function exlog(...content: any) {
  console.log(...content);

  const output = opts.outputElem;

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
  if (outputElem != null) opts.outputElem = outputElem;
}