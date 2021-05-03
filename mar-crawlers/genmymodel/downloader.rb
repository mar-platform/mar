
require_relative 'indexer'
require_relative 'test_dsl'


class Model
  attr_reader :href
  
  def initialize(href, m)
    @href = href
    @json_model = m
  end
  
  def download(driver, folder)
    file_name = to_filename(@href, folder)
    if File.exists?(file_name)
      puts "File #{file_name} already downloaded"
      return false
    else
      driver.download @href, file_name
      return true
    end
  end

  def to_filename(url, folder)
    id = @json_model['project_id']
    if id.nil?
      parts = @href.split("/")
      id = parts[parts.size - 2]
    end
    folder + "/" + id + ".xmi"
  end
end

def download_all(models, folder)
  include TestDSL
  test 'genmymodel' do
    go_to 'https://app.genmymodel.com/api/login'

    fill 'username', 'jesusc'
    fill 'password', 'marcrawler'
  
    submit 'submitlogin' do
      puts "submitted"
    end

    sleep(1)
    
    models.each { |m|
      puts "Downloading #{m.href}"
      if m.href.include?("0bc69cab-6a7b-4cfb-a69b-bcd468d41af4")
        next
      end
      begin
        if m.download(self, folder)
          sleep(1)
        end
      rescue Net::ReadTimeout => e
        puts "Time out. Can't download #{m.href}"
      end
    }
  end
end

if __FILE__ == $0
  if ARGV.size < 2 
    puts "downloader model-type folder [first:last]"
    exit
  end

  init = nil
  last = nil
  if ARGV.size == 3
    parts = ARGV[2].split(":")
    init = parts[0].to_i
    last = parts[1].to_i
  end
  
  model_type = ARGV[0]
  folder = ARGV[1]
  index = make_index(init, last)

  models = index[model_type]

  # links
  # projectId
  # name
  # creationDate
  # lastModificationDate

  to_download = []
  models.each { |m|
    xmi_link = m['links'].find { |l| l['rel'] == 'xmi' }
    unless xmi_link.nil?
      to_download << Model.new(xmi_link['href'], m)
    end
  }

  puts "Collected #{to_download.size} models"
  download_all(to_download, folder)  
end
  
