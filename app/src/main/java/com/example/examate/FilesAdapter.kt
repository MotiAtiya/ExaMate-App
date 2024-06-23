import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.examate.databinding.ItemFileBinding

class FilesAdapter(
    private var files: List<String>,
    private val itemClick: (String) -> Unit,
    private val itemDelete: (String) -> Unit
) : RecyclerView.Adapter<FilesAdapter.FileViewHolder>() {

    inner class FileViewHolder(val binding: ItemFileBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(file: String) {
            binding.fileNameTextView.text = file
            binding.root.setOnClickListener {
                itemClick(file)
            }
            binding.deleteIcon.setOnClickListener {
                itemDelete(file)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val binding = ItemFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int = files.size

    fun updateFiles(newFiles: List<String>) {
        files = newFiles
        notifyDataSetChanged()
    }
}
