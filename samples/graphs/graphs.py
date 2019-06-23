import numpy as np
import pandas as pd
import matplotlib as matplotlib
import matplotlib.pyplot as plt

matplotlib.rcParams['font.size'] = 14
matplotlib.rcParams['savefig.pad_inches'] = 0
plt.style.use('seaborn-notebook')
data = pd.read_csv("kubeznn-2.csv")
time = data['time']

label_resp_time = 'Response Time (s)'
label_workload = 'Workload (reqs/s)'
label_duration = 'Duration (s)'
label_replicas = 'Replicas'

def main():
  response_time()
  workload()
  tuning()
  scaling()
  replicas_compare()

def zero_to_nan(values):
    """Replace every 0 with 'nan' and return a copy."""
    return [float('nan') if x==0 else x for x in values]

def savefig(name, fig):
  fig.tight_layout()
  fig.savefig(name)

def build_resp_time(column):
  fig, plot_resp_time = plt.subplots()
  plot_resp_time.set_ylabel(label_resp_time)
  ay1 = plot_resp_time.plot(time, data[column], label=label_resp_time, alpha=0.8, linestyle='dashed', color='tab:red')
  plot_resp_time.tick_params(axis='y')
  max_resp_time_line(plot_resp_time)
  plot_resp_time.set_xlabel(label_duration)
  # matplotlib.pyplot.yticks(range(0, 11))
  
  return ay1, fig, plot_resp_time

def max_resp_time_line(plot_resp_time):
  plot_resp_time.axhline(y=2, alpha=0.5, linestyle='dashed', color='tab:red', linewidth=0.5)

def workload_totals():
  x = ['Baseline', 'Auto Scaling', 'Auto Tuning']
  totals = [data['base_workload'].sum(), data['scaling_workload'].sum(), data['full_workload'].sum()]
  x_pos = [i for i, _ in enumerate(x)]
  plt.bar(x_pos, totals)
  plt.xlabel("Strategies")
  plt.ylabel("Total requests")
  plt.xticks(x_pos, x)

def tuning():
  ay1, fig, plot_resp_time = build_resp_time('full_resp_time')

  plot_versions = plot_resp_time.twinx()
  plot_versions.set_ylabel(label_replicas)
  ay2 = plot_versions.plot(time, zero_to_nan(data['full_high']), label='High Mode')
  ay3 = plot_versions.plot(time, zero_to_nan(data['full_low']), label='Low Mode')
  ay4 = plot_versions.plot(time, zero_to_nan(data['full_text']), label='Text Mode')
  plot_versions.tick_params(axis='y')
  matplotlib.pyplot.yticks(range(1, 4))

  matplotlib.pyplot.xticks(range(0, 500, 60))
  axis = ay1+ay2+ay3+ay4
  labels = [l.get_label() for l in axis]
  plot_resp_time.legend(axis, labels, loc='upper left')
  fig.set_size_inches(13, 5.2)
  savefig('exp-tuning.png', fig)

def replicas_compare():
  fig, plot_versions = plt.subplots()
  plot_versions.set_ylabel(label_resp_time)
  ay2 = plot_versions.plot(time, data['full_replicas'], label='Auto Tuning')
  ay4 = plot_versions.plot(time, data['scaling_replicas'], label='Auto Scaling')
  plot_versions.tick_params(axis='y')
  max_resp_time_line(plot_versions)
  plot_versions.set_xlabel(label_duration)

  plot_workload = plot_versions.twinx()

  plot_workload.set_ylabel(label_workload)
  ay1 = plot_workload.plot(time, data['full_workload'], label=label_workload, alpha=0.5, linestyle='dashed', color='black')
  plot_workload.tick_params(axis='y')
  matplotlib.pyplot.yticks(range(0, 500, 50))
  matplotlib.pyplot.xticks(range(0, 500, 60))

  axis = ay1+ay2+ay4
  labels = [l.get_label() for l in axis]
  plot_workload.legend(axis, labels, loc='upper left')
  fig.set_size_inches(9, 5.2)
  savefig('exp-replicas.png', fig)

def scaling():
  ay1, fig, plot_resp_time = build_resp_time('scaling_resp_time')

  plot_versions = plot_resp_time.twinx()
  plot_versions.set_ylabel(label_replicas)
  ay2 = plot_versions.plot(time, data['scaling_replicas'], label='High Mode')
  plot_versions.tick_params(axis='y')
  matplotlib.pyplot.yticks(range(1, 4))

  axis = ay1+ay2
  labels = [l.get_label() for l in axis]
  plot_resp_time.legend(axis, labels, loc='upper left')
  fig.set_size_inches(11.7, 5.2)
  savefig('exp-scaling.png', fig)

def workload():
  fig, plot_versions = plt.subplots()
  plot_versions.set_ylabel(label_workload)
  plot_versions.plot(time, data['full_workload'], label=label_workload, alpha=0.5, linestyle='dashed', color='black')
  plot_versions.tick_params(axis='y')
  matplotlib.pyplot.xticks(range(0, 500, 60))
  matplotlib.pyplot.yticks(range(0, 500, 50))
  plot_versions.set_xlabel(label_duration)
  
  fig.tight_layout()
  savefig('exp-workload.png', fig)

def response_time():
  fig, plot_versions = plt.subplots()
  plot_versions.set_ylabel(label_resp_time)
  ay2 = plot_versions.plot(time, data['full_resp_time'], label='Auto Tuning')
  ay3 = plot_versions.plot(time, data['base_resp_time'], label='Baseline')
  ay4 = plot_versions.plot(time, data['scaling_resp_time'], label='Auto Scaling')
  plot_versions.tick_params(axis='y')
  max_resp_time_line(plot_versions)
  plot_versions.set_xlabel(label_duration)

  plot_workload = plot_versions.twinx()

  plot_workload.set_ylabel(label_workload)
  ay1 = plot_workload.plot(time, data['full_workload'], label=label_workload, alpha=0.5, linestyle='dashed', color='black')
  plot_workload.tick_params(axis='y')

  matplotlib.pyplot.yticks(range(0, 500, 50))
  matplotlib.pyplot.xticks(range(0, 500, 60))

  axis = ay1+ay2+ay3+ay4
  labels = [l.get_label() for l in axis]
  plot_workload.legend(axis, labels, loc='best')
  fig.set_size_inches(12, 5.2)
  savefig('exp-all.png', fig)
  

if __name__ == "__main__":
  main()